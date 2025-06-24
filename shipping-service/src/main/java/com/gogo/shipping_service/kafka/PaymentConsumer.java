package com.gogo.shipping_service.kafka;

import com.gogo.base_domaine_service.event.EventStatus;
import com.gogo.base_domaine_service.event.OrderEventDto;
import com.gogo.shipping_service.mapper.ShippingMapper;
import com.gogo.shipping_service.model.Ship;
import com.gogo.shipping_service.service.ShippingService;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class PaymentConsumer {

    @Autowired
    private ShippingService shippingService;

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentConsumer.class);

    @KafkaListener(
            topics = "${spring.kafka.topic.payment.name}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void orderConsumer(OrderEventDto event) {
        if (EventStatus.COMPLETED.name().equalsIgnoreCase(event.getStatus())) {
            LOGGER.info("📦 Shipping service received event => {}", event);


            Ship newShipping = ShippingMapper.mapToShip(event);
            newShipping.setStatus(EventStatus.SHIPPING.name());
            newShipping.setDetails("Order is in shipping status");
            newShipping.setEventTimeStamp(LocalDateTime.now());


            shippingService.saveShip(newShipping);
            LOGGER.info("✅ New shipping record created for order {}", event.getId());
        }


    }
}
