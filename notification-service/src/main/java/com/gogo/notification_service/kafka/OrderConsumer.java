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

        String productName = event.getProductEventDto().getName();
        String username = event.getUserName();

        if (event.getStatus().equalsIgnoreCase(EventStatus.PENDING.name())) {

            // ======= RUPTURE DE STOCK ========
            if (remainingQty == 0) {
                String outOfStockMsg = "Product '" + productName + "' is out of stock!";

                // 🔔 Notification utilisateur
                Notification userNotification = new Notification();
                userNotification.setMessage(outOfStockMsg);
                userNotification.setReadValue(false);
                userNotification.setUsername(username);
                userNotification.setArchived(false);
                notificationRepository.save(userNotification);

                // 🔔 Notification globale (si pas déjà existante)
                if (!notificationRepository.existsByMessageAndUsernameAndReadValueIsFalseAndArchivedIsFalse(outOfStockMsg, "allusers")) {
                    Notification globalNotification = new Notification();
                    globalNotification.setMessage(outOfStockMsg);
                    globalNotification.setReadValue(false);
                    globalNotification.setUsername("allusers");
                    globalNotification.setArchived(false);
                    notificationRepository.save(globalNotification);
                }
            }

            // ======= STOCK FAIBLE ========
            else if (remainingQty < 10) {
                String lowStockMsg = "Product '" + productName + "' stock is low (" + remainingQty + ")";

                // 🔔 Notification utilisateur
                Notification userNotification = new Notification();
                userNotification.setMessage(lowStockMsg);
                userNotification.setReadValue(false);
                userNotification.setUsername(username);
                userNotification.setArchived(false);
                notificationRepository.save(userNotification);

                // 🔔 Notification globale (si pas déjà existante)
                if (!notificationRepository.existsByMessageAndUsernameAndReadValueIsFalseAndArchivedIsFalse(lowStockMsg, "allusers")) {
                    Notification globalNotification = new Notification();
                    globalNotification.setMessage(lowStockMsg);
                    globalNotification.setReadValue(false);
                    globalNotification.setUsername("allusers");
                    globalNotification.setArchived(false);
                    notificationRepository.save(globalNotification);
                }
            }
        }

        // ======= COMMANDE ANNULÉE ========
        else if (event.getStatus().equalsIgnoreCase(EventStatus.CANCELLING.name())) {
            String cancelMsg = "Your order for '" + productName + "' has been cancelled.";

            Notification cancelNotification = new Notification();
            cancelNotification.setMessage(cancelMsg);
            cancelNotification.setReadValue(false);
            cancelNotification.setUsername(username);
            cancelNotification.setArchived(false);
            notificationRepository.save(cancelNotification);
        }

        LOGGER.info("Order event received in Notification service => {}", event);
    }


}
