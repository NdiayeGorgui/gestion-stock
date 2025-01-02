package com.gogo.order_service.kafka;

import com.gogo.base_domaine_service.event.EventStatus;
import com.gogo.base_domaine_service.event.OrderEventDto;
import com.gogo.order_service.model.Order;
import com.gogo.order_service.model.Product;
import com.gogo.order_service.model.ProductItem;
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

        if (event.getStatus().equalsIgnoreCase(EventStatus.CREATED.name())) {

            LOGGER.info("Bill event received in Order service => {}", event);

            Product product = orderService.findProductById(event.getProductEventDto().getProductIdEvent());

            boolean oderExist = orderRepository.existsByOrderIdEventAndOrderStatus(event.getId(), EventStatus.CREATED.name());

            if (oderExist) {

                orderService.updateOrderStatus(event.getId(), EventStatus.CREATED.name());

                LOGGER.info("Order event with created status sent to Inventory service => {}", event);

            }
            //String qtyStatus=event.getProductEventDto().getQtyStatus();
            int qtyUsed = event.getProductItemEventDto().getQty();
            int qr = orderService.qtyRestante(product.getQty(), qtyUsed, event.getStatus());
            if (qr > 0) {
                orderService.updateQuantity(event.getProductEventDto().getProductIdEvent(), qr);
                orderService.updateOrderStatus(event.getId(), EventStatus.CREATED.name());

            }else {
                throw new RuntimeException("Quantite insuffisante");
            }
        }

        if (event.getStatus().equalsIgnoreCase(EventStatus.CANCELED.name())) {

            LOGGER.info("Bill event for cancel order received in Order service => {}", event);

            Order order = orderRepository.findByOrderIdEvent(event.getId());

            boolean oderExist = orderRepository.existsByOrderIdEventAndOrderStatus(order.getOrderIdEvent(), EventStatus.CREATED.name());
            ProductItem productItem = orderService.findProductItemByOrderEventId(event.getId());
            if (oderExist) {
                orderService.updateOrderStatus(event.getId(), event.getStatus());
                int qtyUsed = productItem.getQuantity();
                String productIdEvent= productItem.getProductIdEvent();
                Product product=orderService.findProductById(productIdEvent);
                int qr = orderService.qtyRestante(product.getQty(), qtyUsed, event.getStatus());
               // event.getProductEventDto().set
                event.getProductEventDto().setQty(qr);
               // if (qr > 0) {
                    orderService.updateQuantity(productIdEvent, qr);
                   // orderService.updateOrderStatus(event.getId(), "CREATED");

               // }

                LOGGER.info(String.format("Order event with created status sent to Inventory service => %s", event));
                //   customerProducer.sendMessage(orderEventDto);

            }
        }
    }
}
