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

        int initialQty = event.getProductEventDto().getQty(); // Stock initial
        int orderedQty = event.getProductItemEventDto().getQty(); // Quantité commandée
        int remainingQty = initialQty - orderedQty;

        String productName = event.getProductEventDto().getName();
        String username = event.getUserName();

        if (event.getStatus().equalsIgnoreCase(EventStatus.PENDING.name())) {

            String baseKey = productName.toLowerCase().trim(); // Clé produit

            // === RUPTURE DE STOCK ===
            if (remainingQty == 0) {
                String msg = "Product '" + productName + "' is out of stock!";
                String uniqueKey = baseKey + "_outofstock";

                boolean alreadyExists = notificationRepository
                        .existsByProductKeyAndTypeAndArchivedIsFalseAndReadValueIsFalse(uniqueKey, "outofstock");

                if (!alreadyExists) {
                    Notification globalNotif = new Notification();
                    globalNotif.setMessage(msg);
                    globalNotif.setReadValue(false);
                    globalNotif.setUsername("allusers");
                    globalNotif.setArchived(false);
                    globalNotif.setType("outofstock");
                    globalNotif.setProductKey(uniqueKey); // ⚠️ champ supplémentaire recommandé
                    notificationRepository.save(globalNotif);
                }
            }

            // === STOCK FAIBLE ===
            else if (remainingQty < 10) {
                String msg = "Product '" + productName + "' stock is low (" + remainingQty + ")";
                String uniqueKey = baseKey + "_lowstock";

                boolean alreadyExists = notificationRepository
                        .existsByProductKeyAndTypeAndArchivedIsFalseAndReadValueIsFalse(uniqueKey, "lowstock");

                if (!alreadyExists) {
                    Notification globalNotif = new Notification();
                    globalNotif.setMessage(msg);
                    globalNotif.setReadValue(false);
                    globalNotif.setUsername("allusers");
                    globalNotif.setArchived(false);
                    globalNotif.setType("lowstock");
                    globalNotif.setProductKey(uniqueKey); // ⚠️
                    notificationRepository.save(globalNotif);
                }
            }
        }

        LOGGER.info("Order event received in Notification service => {}", event);
    }



}
