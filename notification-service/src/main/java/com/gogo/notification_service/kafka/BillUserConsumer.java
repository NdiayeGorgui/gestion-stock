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
public class BillUserConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(BillUserConsumer.class);
    private final NotificationRepository notificationRepository;

    public BillUserConsumer(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @KafkaListener(
            topics = "${spring.kafka.topic.billing.name}"
            ,groupId = "${spring.kafka.consumer.group-id}"
    )
    public void orderConsumer(OrderEventDto event) {

        String productName = event.getProductEventDto().getName();
        String username = event.getUserName();
        int initialQty = event.getProductEventDto().getQty(); // Stock initial
        int orderedQty = event.getProductItemEventDto().getQty(); // Quantité commandée
        int remainingQty = initialQty - orderedQty;



        if (event.getStatus().equalsIgnoreCase(EventStatus.CREATED.name())) {

            // === RUPTURE DE STOCK ===
            if (remainingQty == 0) {
                String msg = "Product '" + productName + "' is out of stock!";

                boolean alreadyExists = notificationRepository
                        .existsByMessageAndUsernameAndReadValueIsFalseAndArchivedIsFalse(msg, "allusers");

                if (!alreadyExists) {
                    Notification globalNotif = new Notification();
                    globalNotif.setMessage(msg);
                    globalNotif.setReadValue(false);
                    globalNotif.setUsername("allusers"); // notif globale
                    globalNotif.setArchived(false);
                    globalNotif.setType("global");
                    notificationRepository.save(globalNotif);
                }
            }

            // === STOCK FAIBLE ===
            else if (remainingQty < 10) {
                String msg = "Product '" + productName + "' stock is low (" + remainingQty + ")";

                boolean alreadyExists = notificationRepository
                        .existsByMessageAndUsernameAndReadValueIsFalseAndArchivedIsFalse(msg, "allusers");

                if (!alreadyExists) {
                    Notification globalNotif = new Notification();
                    globalNotif.setMessage(msg);
                    globalNotif.setReadValue(false);
                    globalNotif.setUsername("allusers"); // notif globale
                    globalNotif.setArchived(false);
                    globalNotif.setType("global");
                    notificationRepository.save(globalNotif);
                }
            }
        }

        // === ANNULATION DE COMMANDE — NOTIF SPÉCIFIQUE À L'UTILISATEUR ===
       else  if(event.getStatus().equalsIgnoreCase(EventStatus.CANCELED.name())){
            String msg = "Your order for '" + productName + "' has been cancelled.";

            Notification userNotif = new Notification();
            userNotif.setMessage(msg);
            userNotif.setReadValue(false);
            userNotif.setUsername(username);
            userNotif.setArchived(false);
            userNotif.setType("user");
            notificationRepository.save(userNotif);
        }

        LOGGER.info("Order event received in Notification service => {}", event);
    }

}
