package com.gogo.notification_service.kafka;

import com.gogo.base_domaine_service.event.EventStatus;
import com.gogo.base_domaine_service.event.OrderEventDto;
import com.gogo.base_domaine_service.event.ProductItemEventDto;
import com.gogo.notification_service.model.Notification;
import com.gogo.notification_service.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class OrderConsumerCancel {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderConsumerCancel.class);
    private final NotificationRepository notificationRepository;

    public OrderConsumerCancel(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @KafkaListener(
            topics = "${spring.kafka.topic.billing.name}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void orderConsumer(OrderEventDto event) {
        String username = event.getUserName();
        String orderId = event.getId();

        if (EventStatus.CANCELED.name().equalsIgnoreCase(event.getStatus())) {
            String msg = "Your order: " + orderId + " has been cancelled.";

            Notification userNotif = new Notification();
            userNotif.setMessage(msg);
            userNotif.setReadValue(false);
            userNotif.setUsername(username);
            userNotif.setArchived(false);
            userNotif.setType("user");
            userNotif.setProductKey("order_" + orderId.toLowerCase() + "_canceled");

            notificationRepository.save(userNotif);

            LOGGER.info("📩 Cancelled order notification saved for user: {}", username);
        }

        LOGGER.info("📩 Order event received in Notification service => {}", event);
    }
}

