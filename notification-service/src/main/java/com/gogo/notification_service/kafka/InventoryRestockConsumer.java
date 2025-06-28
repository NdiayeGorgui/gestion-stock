package com.gogo.notification_service.kafka;

import com.gogo.base_domaine_service.dto.Product;
import com.gogo.base_domaine_service.event.EventStatus;
import com.gogo.base_domaine_service.event.OrderEventDto;
import com.gogo.notification_service.model.Notification;
import com.gogo.notification_service.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InventoryRestockConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(InventoryRestockConsumer.class);
    private final NotificationRepository notificationRepository;

    public InventoryRestockConsumer(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @KafkaListener(
            topics = "${spring.kafka.topic.order.name}",
            groupId = "${notification.kafka.group.restock}"
    )
    public void onInventoryUpdate(OrderEventDto event) {
        LOGGER.info("📩 Product UPDATE event received in InventoryRestockConsumer => {}", event);

        if (!EventStatus.UPDATED.name().equalsIgnoreCase(event.getStatus())) {
            LOGGER.warn("🚫 Event status is not UPDATED. Skipping event.");
            return;
        }


        if (event.getProductEventDto() == null) {
            LOGGER.warn("🚫 No products present in ProductEvent.");
            return;
        }

        String productName = event.getProductEventDto().getName();
        if (productName == null || productName.isBlank()) {
            LOGGER.warn("⚠️ Product name empty or null. Ignored.");
            return;
        }

        String baseKey = productName.toLowerCase().trim();
        int qty = event.getProductEventDto().getQty();

        LOGGER.info("🔍 Product analysis : name='{}', qty={}, baseKey='{}'", productName, qty, baseKey);

        boolean hadOutOfStock = notificationRepository
                .existsByProductKeyAndTypeAndArchivedIsFalseAndReadValueIsFalse(baseKey + "_outofstock", "outofstock");
        boolean hadLowStock = notificationRepository
                .existsByProductKeyAndTypeAndArchivedIsFalseAndReadValueIsFalse(baseKey + "_lowstock", "lowstock");
        boolean alreadyRestocked = notificationRepository
                .existsByProductKeyAndTypeAndArchivedIsFalseAndReadValueIsFalse(baseKey + "_restocked", "restocked");

        LOGGER.info("📊 hadOutOfStock={}, hadLowStock={}, alreadyRestocked={}",
                hadOutOfStock, hadLowStock, alreadyRestocked);

        if ((hadLowStock || hadOutOfStock) && qty >= 10 && !alreadyRestocked) {
            Notification notif = new Notification();
            notif.setMessage("Product '" + productName + "' is now available in stock !");
            notif.setType("restocked");
            notif.setProductKey(baseKey + "_restocked");
            notif.setReadValue(false);
            notif.setUsername("allusers");
            notif.setArchived(false);

            notificationRepository.save(notif);
            LOGGER.info("🔔 RESTOCK notification recorded for '{}'", productName);

            // Archive les anciennes notifications
            List<String> keysToArchive = List.of(baseKey + "_lowstock", baseKey + "_outofstock");
            notificationRepository.archiveByProductKeyIn(keysToArchive);
            LOGGER.info("📦 Old archived notifications for '{}'", productName);
        } else {
            LOGGER.info("ℹ️ No RESTOCK notification sent. Conditions not met for '{}'", productName);
        }
    }
}
