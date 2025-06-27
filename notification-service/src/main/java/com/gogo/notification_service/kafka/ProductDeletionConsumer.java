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
public class ProductDeletionConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductDeletionConsumer.class);
    private final NotificationRepository notificationRepository;

    public ProductDeletionConsumer(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @KafkaListener(
            topics = "${spring.kafka.topic.order.name}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void onProductDeleted(OrderEventDto event) {
        LOGGER.info("📩 Product DELETED event received in ProductDeletionConsumer => {}", event);

        if (!EventStatus.DELETED.name().equalsIgnoreCase(event.getStatus())) {
            LOGGER.warn("⛔ Event is not in DELETED status. Skipping.");
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

        String baseKey = productName.toLowerCase().trim() + "_deleted";

        boolean alreadyNotified = notificationRepository
                .existsByProductKeyAndTypeAndArchivedIsFalseAndReadValueIsFalse(baseKey, "deleted");

        if (alreadyNotified) {
            LOGGER.info("🔁 Notification for deleted product '{}' already exists. Skipping.", productName);
            return;
        }

        Notification notif = new Notification();
        notif.setMessage("Product '" + productName + "' has been removed from the catalog !");
        notif.setType("deleted");
        notif.setProductKey(baseKey);
        notif.setReadValue(false);
        notif.setUsername("allusers");
        notif.setArchived(false);

        notificationRepository.save(notif);
        LOGGER.info("✅ Deleted product notification saved for '{}'", productName);
    }
}

