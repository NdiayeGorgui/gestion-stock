package com.gogo.mcp_java_server.kafka;

import com.gogo.base_domaine_service.event.EventStatus;
import com.gogo.base_domaine_service.event.OrderEventDto;
import com.gogo.mcp_java_server.model.Order;
import com.gogo.mcp_java_server.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class OrderConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderConsumer.class);

    @Autowired
    private OrderRepository orderRepository;

    @KafkaListener(
            topics = "${spring.kafka.topic.billing.name}",
            groupId = "${spring.kafka.consumer.bill.order.group-id}"
    )
    public void billConsumer(OrderEventDto event) {
        if (event == null || event.getCustomerEventDto() == null) {
            LOGGER.warn("Received null event or customer details are missing.");
            return;
        }

        String status = event.getStatus().toUpperCase();
        String orderId = event.getId();

        switch (status) {
            case "CREATED" -> {
                Order order = new Order();
                order.setOrderId(orderId);
                order.setOrderStatus(EventStatus.CREATED.name());
                order.setCustomerId(event.getCustomerEventDto().getCustomerIdEvent());
                orderRepository.save(order);
            }

            case "CANCELED", "COMPLETED" -> {
                Order existingOrder = orderRepository.findByOrderId(orderId);
                if (existingOrder != null) {
                    existingOrder.setOrderStatus(status);
                    orderRepository.save(existingOrder);
                } else {
                    LOGGER.warn("Order with ID {} not found for status update.", orderId);
                }
            }

            default -> LOGGER.warn("Unhandled order event status: {}", status);
        }

        LOGGER.info("Order event received in Mpc service => {}", event);
    }
}
