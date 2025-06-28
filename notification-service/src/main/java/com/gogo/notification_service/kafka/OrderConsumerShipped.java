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
public class OrderConsumerShipped {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderConsumerShipped.class);
    private final NotificationRepository notificationRepository;

    public OrderConsumerShipped(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @KafkaListener(
            topics = "${spring.kafka.topic.shipping.name}",
            groupId = "${notification.kafka.group.shipped}"
    )
    public void orderConsumer(OrderEventDto event) {
        String username = event.getUserName();
        String orderId = event.getId();

        if (EventStatus.SHIPPED.name().equalsIgnoreCase(event.getStatus())) {
            String msg = "The order: " + orderId + " has been shipped successfully!";

            Notification userNotif = new Notification();
            userNotif.setMessage(msg);
            userNotif.setReadValue(false);
            userNotif.setUsername(username);
            userNotif.setArchived(false);
            userNotif.setType("user");
            userNotif.setProductKey("order_" + orderId.toLowerCase() + "_shipped");

            notificationRepository.save(userNotif);

            LOGGER.info("📦 Shipped order notification saved for user: {}", username);
        }

        LOGGER.info("📦 Order event received in Notification service => {}", event);
    }
}
