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
        if (EventStatus.SHIPPED.name().equalsIgnoreCase(event.getStatus())) {  // ✅ CORRECT

            String orderId = event.getId();
            String paymentId = event.getPaymentId();

            // Création de l'entrée "DELIVERING"
            Delivered delivered = new Delivered();
            delivered.setOrderId(orderId);
            delivered.setPaymentId(paymentId);
            delivered.setCustomerId(event.getCustomerEventDto().getCustomerIdEvent());
            delivered.setCustomerName(event.getCustomerEventDto().getName());
            delivered.setCustomerMail(event.getCustomerEventDto().getEmail());
            delivered.setStatus(EventStatus.DELIVERING.name());
            delivered.setEventTimeStamp(LocalDateTime.now());
            delivered.setDetails("Order is in delivering status");

            deliveredCommandService.saveDeliveredCommand(delivered);

            // Mise à jour de l'event et envoi
            event.setStatus(EventStatus.DELIVERING.name());
            deliveredCommandProducer.sendMessage(event);

            LOGGER.info("📦 Order {} moved to DELIVERING. Event sent.", orderId);
        }
    }

}
