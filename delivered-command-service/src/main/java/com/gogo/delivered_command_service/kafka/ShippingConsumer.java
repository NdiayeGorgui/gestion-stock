package com.gogo.delivered_command_service.kafka;

import com.gogo.base_domaine_service.event.EventStatus;
import com.gogo.base_domaine_service.event.OrderEventDto;
import com.gogo.delivered_command_service.model.Delivered;
import com.gogo.delivered_command_service.service.DeliveredCommandService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;


@Service
public class ShippingConsumer {

    @Autowired
    private DeliveredCommandService deliveredCommandService;
    @Autowired
    private DeliveredCommandProducer deliveredCommandProducer;

    private static final Logger LOGGER = LoggerFactory.getLogger(ShippingConsumer.class);

    @KafkaListener(
            topics = "${spring.kafka.topic.shipping.name}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumeOrderShip(OrderEventDto event) {
        if (event.getStatus().equalsIgnoreCase(EventStatus.SHIPPED.name())) {
        	

            // Vérifier s'il y a déjà une commande en DELIVERING ou DELIVERED
            boolean isAlreadyProcessed = deliveredCommandService.isOrderAlreadyProcessed(event.getPaymentId());
            boolean isAlreadyProcessed2 = deliveredCommandService.isOrderAlreadyProcessed2(event.getPaymentId(),event.getId());
            

            // Récupérer la liste des commandes déjà en DELIVERING
            List<Delivered> deliveredList = deliveredCommandService.findByPaymentAndStatus2(event.getPaymentId(),event.getId(), EventStatus.DELIVERING.name());

            // Si aucune commande n'est encore en DELIVERING, en ajouter une nouvelle
            if (deliveredList.isEmpty()) {
                Delivered newDelivered = new Delivered();
                newDelivered.setOrderId(event.getId());
                newDelivered.setPaymentId(event.getPaymentId());
                newDelivered.setCustomerId(event.getCustomerEventDto().getCustomerIdEvent());
                newDelivered.setCustomerName(event.getCustomerEventDto().getName());
                newDelivered.setCustomerMail(event.getCustomerEventDto().getEmail());
                newDelivered.setStatus(EventStatus.DELIVERING.name());
                newDelivered.setEventTimeStamp(LocalDateTime.now());
                newDelivered.setDetails("Order is in delivering status");

                deliveredCommandService.saveDeliveredCommand(newDelivered);
            }

            // Si une commande existe déjà en DELIVERING mais pas en DELIVERED, on l'update
            if (!isAlreadyProcessed2) {
                for (Delivered orderDelivered : deliveredList) {
                    orderDelivered.setOrderId(event.getId());
                    orderDelivered.setPaymentId(event.getPaymentId());
                    orderDelivered.setCustomerId(event.getCustomerEventDto().getCustomerIdEvent());
                    orderDelivered.setCustomerName(event.getCustomerEventDto().getName());
                    orderDelivered.setCustomerMail(event.getCustomerEventDto().getEmail());
                    orderDelivered.setStatus(EventStatus.DELIVERING.name());
                    orderDelivered.setEventTimeStamp(LocalDateTime.now());
                    orderDelivered.setDetails("Order is in delivering status");
                    deliveredCommandService.saveDeliveredCommand(orderDelivered);
                }
            }

            // Mise à jour de l'événement et envoi
            event.setStatus(EventStatus.DELIVERING.name());
            deliveredCommandProducer.sendMessage(event);

            LOGGER.info("Order event received in Delivered command service with delivering status => {}", event);
        }
    }

}

