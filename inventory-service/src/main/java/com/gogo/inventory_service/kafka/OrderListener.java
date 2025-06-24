
package com.gogo.inventory_service.kafka;

import com.gogo.base_domaine_service.event.EventStatus;
import com.gogo.base_domaine_service.event.OrderEventDto;
import com.gogo.base_domaine_service.event.ProductItemEventDto;
import com.gogo.inventory_service.model.ProductModel;
import com.gogo.inventory_service.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class OrderListener {

    @Autowired
    private ProductService productService;
    @Autowired
    private OrderProducer orderProducer;

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderListener.class);

    @KafkaListener(
            topics = "${spring.kafka.topic.billing.name}",
            groupId = "${spring.kafka.update.bill.consumer.group-id}"
    )
    public void consumeProductStatus(OrderEventDto event) {
        LOGGER.info("📦 Order event received in Inventory service => {}", event);

        // Création : on diminue les quantités
        if (EventStatus.CREATED.name().equalsIgnoreCase(event.getStatus())) {
            for (ProductItemEventDto item : event.getProductItemEventDtos()) {
                ProductModel product = productService.findProductById(item.getProductIdEvent());

                if (product == null) {
                    LOGGER.warn("🚫 Produit introuvable: {}", item.getProductIdEvent());
                    continue;
                }

                int qtyBefore = product.getQty();
                int qtyUsed = item.getQty();
                int qtyAfter = productService.qtyRestante(qtyBefore, qtyUsed, EventStatus.CREATED.name());

                if (qtyAfter >= 0) {
                    productService.updateProductQty(item.getProductIdEvent(), qtyAfter);
                    LOGGER.info("✅ Stock décrémenté pour {}: {} -> {}", item.getProductIdEvent(), qtyBefore, qtyAfter);
                } else {
                    LOGGER.error("❌ Stock insuffisant pour le produit {} (dispo={}, demandé={})", item.getProductIdEvent(), qtyBefore, qtyUsed);
                    throw new RuntimeException("Quantité insuffisante pour le produit : " + item.getProductIdEvent());
                }
            }
        }

        // Annulation : on restaure les quantités
        else if (EventStatus.CANCELED.name().equalsIgnoreCase(event.getStatus())) {
            for (ProductItemEventDto item : event.getProductItemEventDtos()) {
                ProductModel product = productService.findProductById(item.getProductIdEvent());

                if (product == null) {
                    LOGGER.warn("🚫 Produit introuvable: {}", item.getProductIdEvent());
                    continue;
                }

                int qtyBefore = product.getQty();
                int qtyRestored = item.getQty();
                int qtyAfter = productService.qtyRestante(qtyBefore, qtyRestored, EventStatus.CANCELED.name());

                productService.updateProductQty(item.getProductIdEvent(), qtyAfter);
                LOGGER.info("🔄 Stock restauré pour {}: {} -> {}", item.getProductIdEvent(), qtyBefore, qtyAfter);
            }
        }
    }

}

