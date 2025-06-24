package com.gogo.notification_service.kafka;

import com.gogo.base_domaine_service.event.EventStatus;
import com.gogo.base_domaine_service.event.OrderEventDto;
import com.gogo.notification_service.dto.OrderEventForNotificationDto;
import com.gogo.notification_service.dto.ProductItemNotificationDto;
import com.gogo.notification_service.mapper.NotificationMapper;
import com.gogo.notification_service.model.Notification;
import com.gogo.notification_service.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderConsumer.class);
    private final NotificationRepository notificationRepository;

    public OrderConsumer(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @KafkaListener(
            topics = "${spring.kafka.topic.order.name}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void orderConsumer(OrderEventDto event) {
        LOGGER.info("📩 Order event received in Notification service => {}", event);

        if (!EventStatus.PENDING.name().equalsIgnoreCase(event.getStatus())) {
            LOGGER.warn("Event is not in PENDING status, skipping.");
            return;
        }

        // ✅ Mapper vers ton DTO enrichi avec initialQty
        OrderEventForNotificationDto mapped = NotificationMapper.mapToLocalOrderEvent(event);
        List<ProductItemNotificationDto> items = mapped.getProductItemEventDtos();

        if (items == null || items.isEmpty()) {
            LOGGER.warn("No product items in event, skipping.");
            return;
        }

        for (ProductItemNotificationDto item : items) {
            String productName = item.getProductName();
            if (productName == null) {
                LOGGER.warn("⚠️ Product name is null for item: {}, skipping stock logic.", item);
                continue;
            }

            String baseKey = productName.toLowerCase().trim();
            int orderedQty = item.getQty();
            int initialQty = item.getInitialQty();
            int remainingQty = initialQty - orderedQty;

            // === OUT OF STOCK ===
            if (remainingQty <= 0) {
                String uniqueKey = baseKey + "_outofstock";
                boolean exists = notificationRepository
                        .existsByProductKeyAndTypeAndArchivedIsFalseAndReadValueIsFalse(uniqueKey, "outofstock");

                if (!exists) {
                    Notification notif = new Notification();
                    notif.setMessage("Product '" + productName + "' is out of stock!");
                    notif.setType("outofstock");
                    notif.setProductKey(uniqueKey);
                    notif.setReadValue(false);
                    notif.setUsername("allusers");
                    notif.setArchived(false);
                    notificationRepository.save(notif);
                    LOGGER.info("🔔 Out-of-stock notification saved: {}", uniqueKey);
                }
            }

            // === LOW STOCK ===
            else if (remainingQty < 10) {
                String uniqueKey = baseKey + "_lowstock";
                boolean exists = notificationRepository
                        .existsByProductKeyAndTypeAndArchivedIsFalseAndReadValueIsFalse(uniqueKey, "lowstock");

                if (!exists) {
                    Notification notif = new Notification();
                    notif.setMessage("Product '" + productName + "' stock is low (" + remainingQty + ")");
                    notif.setType("lowstock");
                    notif.setProductKey(uniqueKey);
                    notif.setReadValue(false);
                    notif.setUsername("allusers");
                    notif.setArchived(false);
                    notificationRepository.save(notif);
                    LOGGER.info("🔔 Low-stock notification saved: {}", uniqueKey);
                }
            }
        }
    }
}
