package com.gogo.notification_service.kafka;

import com.gogo.base_domaine_service.event.EventStatus;
import com.gogo.base_domaine_service.event.OrderEventDto;
import com.gogo.notification_service.model.Notification;
import com.gogo.notification_service.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class ProductCreationConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductCreationConsumer.class);
    private final NotificationRepository notificationRepository;

    public ProductCreationConsumer(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @KafkaListener(
            topics = "${spring.kafka.topic.order.name}",
            groupId = "${notification.kafka.group.creation}"
    )
    public void onProductCreated(OrderEventDto event) {
        LOGGER.info("📩 Product CREATED event received in ProductCreationConsumer => {}", event);

        if (!EventStatus.CREATED.name().equalsIgnoreCase(event.getStatus())) {
            LOGGER.warn("⛔ Event is not in CREATED status. Skipping.");
            return;
        }

        if (event.getProductEventDto() == null) {
            LOGGER.warn("🚫 No product found in event.");
            return;
        }

        String productName = event.getProductEventDto().getName();
        if (productName == null || productName.isBlank()) {
            LOGGER.warn("⚠️ Product name is null or blank.");
            return;
        }

        String baseKey = productName.toLowerCase().trim() + "_new";

        boolean alreadyNotified = notificationRepository
                .existsByProductKeyAndTypeAndArchivedIsFalseAndReadValueIsFalse(baseKey, "newproduct");

        if (alreadyNotified) {
            LOGGER.info("🔁 Notification for new product '{}' already exists. Skipping.", productName);
            return;
        }

        Notification notif = new Notification();
        notif.setMessage("New product available : '" + productName + "' !");
        notif.setType("newproduct");
        notif.setProductKey(baseKey);
        notif.setReadValue(false);
        notif.setUsername("allusers");
        notif.setArchived(false);

        notificationRepository.save(notif);
        LOGGER.info("✅ New product notification saved for '{}'", productName);
    }
}
