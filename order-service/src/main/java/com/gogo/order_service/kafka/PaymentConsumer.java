package com.gogo.order_service.kafka;

import com.gogo.base_domaine_service.event.EventStatus;
import com.gogo.base_domaine_service.event.OrderEventDto;
import com.gogo.order_service.model.Order;
import com.gogo.order_service.model.OrderEventSourcing;
import com.gogo.order_service.repository.OrderRepository;
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
            topics = "${spring.kafka.topic.payment.name}"
            , groupId = "${spring.kafka.consumer.payment.group-id}"
    )
    public void consumeProductStatus(OrderEventDto event) {

        if (event.getStatus().equalsIgnoreCase(EventStatus.COMPLETED.name())) {

            List<Order> orders=orderService.findByCustomer(event.getCustomerEventDto().getCustomerIdEvent());
            for (Order order:orders){
                if(order.getOrderStatus().equalsIgnoreCase(EventStatus.CREATED.name())){
                    order.setOrderStatus(EventStatus.COMPLETED.name());
                    orderService.saveOrder(order);
                    event.setPaymentId(order.getOrderId());

                }
            }
            //save the event sourcing table with confirmed status
            List<OrderEventSourcing> orderList = orderService.orderEventSourcingList(EventStatus.CREATED.name(), event.getCustomerEventDto().getCustomerIdEvent());

            if (orderList != null) {
                for (OrderEventSourcing order : orderList) {
                    // Vérifier si cette commande est déjà confirmée, annulée, expédiée ou délivrée
                    boolean isAlreadyProcessed = orderService.isOrderAlreadyProcessed(order.getOrderId());

                    if (!isAlreadyProcessed) {
                        OrderEventSourcing orderEventSourcing = new OrderEventSourcing();
                        orderEventSourcing.setOrderId(order.getOrderId());
                        orderEventSourcing.setCustomerId(order.getCustomerId());
                        orderEventSourcing.setStatus(EventStatus.CONFIRMED.name());
                        orderEventSourcing.setEventTimeStamp(LocalDateTime.now());
                        orderEventSourcing.setDetails("Order Confirmed");

                        orderService.saveOrderEventModel(orderEventSourcing);

                        event.setId(order.getOrderId());
                        event.setStatus(EventStatus.CONFIRMED.name());
                        //event.setPaymentId(order.getOrderId());
                        orderProducer.sendMessage(event);
                        LOGGER.info("Oder event sent with confirmed status => {}", event);
                    }
                }
            }
        }
    }
}

