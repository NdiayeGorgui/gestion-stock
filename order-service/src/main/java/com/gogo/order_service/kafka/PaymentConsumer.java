package com.gogo.order_service.kafka;

import com.gogo.base_domaine_service.event.EventStatus;
import com.gogo.base_domaine_service.event.OrderEventDto;
import com.gogo.order_service.model.Order;
import com.gogo.order_service.model.OrderEventSourcing;
import com.gogo.order_service.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PaymentConsumer {

    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderProducer orderProducer;



    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentConsumer.class);

    @KafkaListener(
            topics = "${spring.kafka.topic.payment.name}",
            groupId = "${spring.kafka.consumer.payment.group-id}"
    )
    public void paymentConsumer(OrderEventDto event) {
        if (!EventStatus.COMPLETED.name().equalsIgnoreCase(event.getStatus())) return;

        String customerId = event.getCustomerEventDto().getCustomerIdEvent();
        List<Order> orders = orderService.findByCustomer(customerId);

        for (Order order : orders) {
            if (EventStatus.CREATED.name().equalsIgnoreCase(order.getOrderStatus())) {

                // 1. Mettre à jour l’état de la commande
                order.setOrderStatus(EventStatus.COMPLETED.name());
                orderService.saveOrder(order);

                // 2. Enregistrer un événement CONFIRMED dans la table d’event sourcing
                OrderEventSourcing eventSourcing = new OrderEventSourcing();
                eventSourcing.setOrderId(order.getOrderIdEvent());
                eventSourcing.setCustomerId(order.getCustomerIdEvent());
                eventSourcing.setStatus(EventStatus.CONFIRMED.name());
                eventSourcing.setEventTimeStamp(LocalDateTime.now());
                eventSourcing.setDetails("Order confirmed after payment.");

                orderService.saveOrderEventModel(eventSourcing);

                // 3. Envoyer l’événement pour que d’autres services soient au courant
                event.setId(order.getOrderIdEvent());
                event.getCustomerEventDto().setName(order.getCustomer().getName());
                event.setStatus(EventStatus.CONFIRMED.name());
                LOGGER.info("✅ Order {} completed and event sent", event);
                orderProducer.sendMessage(event);

                LOGGER.info("✅ Order {} confirmed and event sourcing saved", order.getOrderIdEvent());
            }


        }

    }

}

