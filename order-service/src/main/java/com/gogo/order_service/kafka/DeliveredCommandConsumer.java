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
public class DeliveredCommandConsumer {

    @Autowired
    private  OrderService orderService;


    private static final Logger LOGGER = LoggerFactory.getLogger(DeliveredCommandConsumer.class);

    @KafkaListener(
            topics = "${spring.kafka.topic.delivered.name}"
            , groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumeProductStatus(OrderEventDto event) {

        //save the event sourcing table with delivered status
        if (event.getStatus().equalsIgnoreCase(EventStatus.DELIVERED.name())) {

            OrderEventSourcing orderEventSourcing=new OrderEventSourcing();
            orderEventSourcing.setOrderId(event.getId());
            orderEventSourcing.setCustomerId(event.getCustomerEventDto().getCustomerIdEvent());
            orderEventSourcing.setStatus(EventStatus.DELIVERED.name());
            orderEventSourcing.setEventTimeStamp(LocalDateTime.now());
            orderEventSourcing.setDetails("Order Delivered");
            orderService.saveOrderEventModel(orderEventSourcing);

            LOGGER.info("Oder event received in order service with delivered status => {}", event);

        }


    }
}

