package com.gogo.notification_service.kafka;

import com.gogo.base_domaine_service.event.EventStatus;
import com.gogo.base_domaine_service.event.OrderEventDto;
import com.gogo.notification_service.model.Notification;
import com.gogo.notification_service.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

        int initialQty = event.getProductEventDto().getQty(); // stock avant la commande
        int orderedQty = event.getProductItemEventDto().getQty(); // quantité commandée
        int remainingQty = initialQty - orderedQty;

        if (event.getStatus().equalsIgnoreCase(EventStatus.PENDING.name())) {

            if (remainingQty == 0) {
                Notification notification = new Notification();
                notification.setMessage("Product '" + event.getProductEventDto().getName() + "' is OUT OF STOCK!");
                notification.setReadValue(false);
                notification.setUsername(event.getUserName()); // tous les utilisateurs
                notification.setArchived(false);
                notificationRepository.save(notification);
            }

            else if (remainingQty < 10) {
                Notification notification = new Notification();
                notification.setMessage("Product '" + event.getProductEventDto().getName() + "' stock is LOW (" + remainingQty + ")");
                notification.setReadValue(false);
                notification.setUsername(event.getUserName()); // tous les utilisateurs
                notification.setArchived(false);
                notificationRepository.save(notification);
            }
        }

        LOGGER.info("Order event received in Notification service => {}", event);
    }

}
