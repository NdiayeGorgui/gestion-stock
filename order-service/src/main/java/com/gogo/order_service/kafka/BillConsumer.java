package com.gogo.order_service.kafka;

import com.gogo.base_domaine_service.event.OrderEventDto;
import com.gogo.order_service.model.Order;
import com.gogo.order_service.model.Product;
import com.gogo.order_service.repository.OrderRepository;
import com.gogo.order_service.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class BillConsumer {
    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private KafkaTemplate<String, OrderEventDto> updateKafkaTemplate;

    private static final Logger LOGGER = LoggerFactory.getLogger(BillConsumer.class);

    @KafkaListener(
            topics = "${spring.kafka.topic.billing.name}"
            , groupId = "${spring.kafka.consumer.bill.group-id}"
    )
    public void consumeBill(OrderEventDto event) {
        String STATUS = "CREATED";

        if (event.getStatus().equalsIgnoreCase("CREATED")) {

            LOGGER.info(String.format("Bill event received in Order service => %s", event));

            Product product = orderService.findProductById(event.getProductEventDto().getProductIdEvent());

            boolean oderExist = orderRepository.existsByOrderIdEventAndOrderStatus(event.getId(), STATUS);

            if (oderExist) {

                orderService.updateOrderStatus(event.getId(), "CREATE");

                LOGGER.info(String.format("Order event with created status sent to Inventory service => %s", event));

            }
            int qtyUsed = event.getProductItemEventDto().getQty();
            int qr = orderService.qtyRestante(product.getQty(), qtyUsed);
            if (qr > 0) {
                orderService.updateQuantity(event.getProductEventDto().getProductIdEvent(), qr);
                orderService.updateOrderStatus(event.getId(), "CREATED");

            }

        }

        if (event.getStatus().equalsIgnoreCase("CANCELED")) {

            LOGGER.info(String.format("Bill event for cancel order received in Order service => %s", event));

            Order order = orderRepository.findByOrderIdEvent(event.getId());

            boolean oderExist = orderRepository.existsByOrderIdEventAndOrderStatus(order.getOrderIdEvent(), STATUS);

            if (oderExist) {

                orderService.updateOrderStatus(event.getId(), event.getStatus());

                LOGGER.info(String.format("Order event with created status sent to Inventory service => %s", event));
                //   customerProducer.sendMessage(orderEventDto);

            }
        }
    }
}
