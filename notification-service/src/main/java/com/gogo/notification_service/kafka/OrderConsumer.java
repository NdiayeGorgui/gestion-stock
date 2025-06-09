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
        if (!EventStatus.PENDING.name().equalsIgnoreCase(event.getStatus())) {
            return; // Ignorer les statuts non pertinents
        }

        String productName = event.getProductEventDto().getName();
        int availableQty = event.getProductEventDto().getQty();
        int initialQty = event.getProductItemEventDto().getQty();
        String message = null;

        if (availableQty == 0 || availableQty == initialQty) {
            message = "Product stock " + productName + " is out of stock!";
        } else if (availableQty < 10) {
            message = "Product stock " + productName + " is low!";
        }

        if (message != null) {
            Notification notification = new Notification();
            notification.setMessage(message);
            notification.setReadValue(false);
            notification.setUsername(event.getUserName());
            notification.setArchived(false);

            notificationRepository.save(notification);
            LOGGER.info("Notification saved: {}", message);
        }

        LOGGER.info("Order event received in Notification service => {}", event);
    }
}
