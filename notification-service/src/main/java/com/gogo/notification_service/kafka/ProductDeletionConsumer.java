package com.gogo.notification_service.kafka;

import com.gogo.base_domaine_service.event.EventStatus;
import com.gogo.base_domaine_service.event.OrderEventDto;
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
            groupId = "${notification.kafka.group.deletion}"
    )
    public void onProductDeleted(OrderEventDto event) {
        LOGGER.info("🗑️ Product DELETED event received => {}", event);

        if (!EventStatus.DELETED.name().equalsIgnoreCase(event.getStatus())) {
            LOGGER.warn("⛔ Skipping: status {} is not DELETED", event.getStatus());
            return;
        }

        if (event.getProductEventDto() == null) {
            LOGGER.warn("🚫 Skipping: productEventDto is null.");
            return;
        }

        String productName = event.getProductEventDto().getName();
        if (productName == null || productName.isBlank()) {
            LOGGER.warn("⚠️ Product name is empty or null.");
            return;
        }

        String baseKey = productName.toLowerCase().trim();
        notificationRepository.archiveByProductKeyIn(
                java.util.List.of(baseKey + "_lowstock", baseKey + "_outofstock", baseKey + "_restocked")
        );

        LOGGER.info("📦 Notifications liées au produit '{}' archivées avec succès", productName);
    }
}
