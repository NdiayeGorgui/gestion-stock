package com.gogo.order_service.kafka;

import com.gogo.base_domaine_service.event.EventStatus;
import com.gogo.base_domaine_service.event.OrderEventDto;
import com.gogo.order_service.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class PaymentConsumer {

    @Autowired
    OrderService orderService;

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentConsumer.class);

    @KafkaListener(
            topics = "${spring.kafka.topic.payment.name}"
            , groupId = "${spring.kafka.consumer.payment.group-id}"
    )
    public void consumeProductStatus(OrderEventDto event) {

        if (event.getStatus().equalsIgnoreCase(EventStatus.COMPLETED.name())) {
            event.setStatus(EventStatus.COMPLETED.name());
           // orderService.updateOrderStatus(event.getId(), event.getStatus());
            orderService.updateAllOrderStatus(event.getCustomerEventDto().getCustomerIdEvent(), event.getStatus());
            LOGGER.info("Payment Update event with completed status sent to Order service => {}", event);
            //  billProducer.sendMessage(event);
        }
    }
}

