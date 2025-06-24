package com.gogo.order_service.kafka;

import com.gogo.base_domaine_service.event.EventStatus;
import com.gogo.base_domaine_service.event.OrderEventDto;
import com.gogo.base_domaine_service.event.ProductItemEventDto;
import com.gogo.order_service.model.Order;
import com.gogo.order_service.model.OrderEventSourcing;
import com.gogo.order_service.model.Product;
import com.gogo.order_service.model.ProductItem;
import com.gogo.order_service.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class BillConsumer {

    @Autowired
    private OrderService orderService;

    private static final Logger LOGGER = LoggerFactory.getLogger(BillConsumer.class);

    @KafkaListener(
            topics = "${spring.kafka.topic.billing.name}",
            groupId = "${spring.kafka.consumer.bill.group-id}"
    )
    public void consumeBill(OrderEventDto event) {
        LOGGER.info("📥 Event reçu dans OrderService => {}", event);
        String orderIdEvent = event.getId();

        Order order = orderService.findOrderByOrderRef(orderIdEvent);
        if (order == null) {
            LOGGER.warn("⚠️ Commande non trouvée: {}", orderIdEvent);
            return;
        }

        // Si CREATED
        if (EventStatus.CREATED.name().equalsIgnoreCase(event.getStatus())) {
            if (!order.getOrderStatus().equalsIgnoreCase(EventStatus.PENDING.name())) {
                LOGGER.warn("⚠️ Commande non dans l'état PENDING: {}", orderIdEvent);
                return;
            }

            // MAJ statut
            orderService.updateOrderStatus(orderIdEvent, EventStatus.CREATED.name());

            // MAJ stock
            for (ProductItemEventDto item : event.getProductItemEventDtos()) {
                Product product = orderService.findProductById(item.getProductIdEvent());
                int qtyRestante = orderService.qtyRestante(product.getQty(), item.getQty(), EventStatus.CREATED.name());
                orderService.updateQuantity(product.getProductIdEvent(), qtyRestante);
            }

            // Sauvegarder l’événement
            saveOrderEvent(order, EventStatus.CREATED.name(), "Order created.");
            LOGGER.info("✅ Commande {} marquée comme CREATED", orderIdEvent);
        }

        // Si CANCELED
        else if (EventStatus.CANCELED.name().equalsIgnoreCase(event.getStatus())) {
            if (!order.getOrderStatus().equalsIgnoreCase(EventStatus.CREATED.name())) {
                LOGGER.warn("⚠️ Impossible d'annuler une commande non créee: {}", orderIdEvent);
                return;
            }

            orderService.updateOrderStatus(orderIdEvent, EventStatus.CANCELED.name());

            // Restituer les quantités
            for (ProductItemEventDto item : event.getProductItemEventDtos()) {
                Product product = orderService.findProductById(item.getProductIdEvent());
                ProductItem productItem = orderService.findProductItemByOrderIdEventAndProductIdEvent(orderIdEvent, item.getProductIdEvent());
                int qtyRestante = orderService.qtyRestante(product.getQty(), productItem.getQuantity(), EventStatus.CANCELED.name());
                orderService.updateQuantity(product.getProductIdEvent(), qtyRestante);
            }

            saveOrderEvent(order, EventStatus.CANCELED.name(), "Order canceled.");
            LOGGER.info("❌ Commande {} annulée", orderIdEvent);
        }

        // TODO: Ajouter SHIPPED, DELIVERED ici plus tard
    }

    private void saveOrderEvent(Order order, String status, String details) {
        OrderEventSourcing event = new OrderEventSourcing();
        event.setOrderId(order.getOrderIdEvent());
        event.setCustomerId(order.getCustomerIdEvent());
        event.setStatus(status);
        event.setEventTimeStamp(LocalDateTime.now());
        event.setDetails(details);

        orderService.saveOrderEventModel(event);
    }


}
