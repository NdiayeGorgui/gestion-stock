package com.gogo.order_service.kafka;

import com.gogo.base_domaine_service.event.EventStatus;
import com.gogo.base_domaine_service.event.OrderEventDto;
import com.gogo.order_service.model.OrderEventSourcing;
import com.gogo.order_service.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


@Service
public class ShippingConsumer {

    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderProducer orderProducer;

    private static final Logger LOGGER = LoggerFactory.getLogger(ShippingConsumer.class);

    @KafkaListener(
            topics = "${spring.kafka.topic.shipping.name}"
            , groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumeProductStatus(OrderEventDto event) {

        //save the event sourcing table with shipped status
        if (event.getStatus().equalsIgnoreCase(EventStatus.SHIPPED.name())) {

                OrderEventSourcing orderEventSourcing=new OrderEventSourcing();
                orderEventSourcing.setOrderId(event.getId());
                orderEventSourcing.setCustomerId(event.getCustomerEventDto().getCustomerIdEvent());
                orderEventSourcing.setStatus(EventStatus.SHIPPED.name());
                orderEventSourcing.setEventTimeStamp(LocalDateTime.now());
                orderEventSourcing.setDetails("Order Shipped");
                orderService.saveOrderEventModel(orderEventSourcing);

            LOGGER.info("Oder event received in order service with shipped status => {}", event);

        }
    }
}

