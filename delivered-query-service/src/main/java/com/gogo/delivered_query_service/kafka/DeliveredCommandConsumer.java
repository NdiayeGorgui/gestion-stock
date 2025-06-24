package com.gogo.delivered_query_service.kafka;

import com.gogo.base_domaine_service.event.CustomerEventDto;
import com.gogo.base_domaine_service.event.EventStatus;
import com.gogo.base_domaine_service.event.OrderEventDto;
import com.gogo.delivered_query_service.model.Delivered;
import com.gogo.delivered_query_service.service.DeliveredQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class DeliveredCommandConsumer {

    @Autowired
    private DeliveredQueryService deliveredQueryService;

    private static final Logger LOGGER = LoggerFactory.getLogger(DeliveredCommandConsumer.class);

    @KafkaListener(
            topics = "${spring.kafka.topic.delivered.name}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumeDeliveredEvent(OrderEventDto event) {

        String orderId = event.getId();
        String paymentId = event.getPaymentId();
        CustomerEventDto customer = event.getCustomerEventDto();

        if (event.getStatus().equalsIgnoreCase(EventStatus.DELIVERING.name())) {

            boolean alreadyDelivering = deliveredQueryService.existsByOrderIdAndStatus(orderId, EventStatus.DELIVERING.name());
            boolean alreadyDelivered = deliveredQueryService.existsByOrderIdAndStatus(orderId, EventStatus.DELIVERED.name());


                Delivered delivered = new Delivered();
                delivered.setOrderId(orderId);
                delivered.setPaymentId(paymentId);
                delivered.setCustomerId(customer.getCustomerIdEvent());
                delivered.setCustomerName(customer.getName());
                delivered.setCustomerMail(customer.getEmail());
                delivered.setStatus(EventStatus.DELIVERING.name());
                delivered.setEventTimeStamp(LocalDateTime.now());
                delivered.setDetails("Order is in delivering status");

                deliveredQueryService.saveDeliveredQuery(delivered);
                LOGGER.info("✅ Order marked as DELIVERING in Delivered Query Service => {}", orderId);


        } else if (event.getStatus().equalsIgnoreCase(EventStatus.DELIVERED.name())) {

            Delivered existing = deliveredQueryService
                    .findByOrderIdAndStatus(orderId, EventStatus.DELIVERING.name())
                    .orElseThrow(() -> new RuntimeException("No delivering order found for id: " + orderId));

            existing.setStatus(EventStatus.DELIVERED.name());
            existing.setDetails("Order is delivered");
            existing.setEventTimeStamp(LocalDateTime.now());
            // On garde paymentId déjà existant dans l’objet
            deliveredQueryService.saveDeliveredQuery(existing);

            LOGGER.info("✅ Order marked as DELIVERED in Delivered Query Service => {}", orderId);
        }
    }
}
