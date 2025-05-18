
package com.gogo.inventory_service.kafka;

import com.gogo.base_domaine_service.event.EventStatus;
import com.gogo.base_domaine_service.event.OrderEventDto;
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
            topics = "${spring.kafka.topic.billing.name}"
            ,groupId = "${spring.kafka.update.bill.consumer.group-id}"
    )
    public void consumeProductStatus(OrderEventDto event){

        if (event.getStatus().equalsIgnoreCase(EventStatus.CREATED.name())) {
            ProductModel product=productService.findProductById(event.getProductEventDto().getProductIdEvent());
            product.setQtyStatus(event.getProductEventDto().getQtyStatus());
            productService.saveProduct(product);
            int qtyUsed= event.getProductItemEventDto().getQty();
            int qr=productService.qtyRestante(product.getQty(),qtyUsed,event.getStatus());
            if(qr>0){
                productService.updateProductQty(event.getProductEventDto().getProductIdEvent(),qr);

                LOGGER.info("Product Update event with updated quantity status sent to order service => {}", event);
               // orderProducer.sendMessage(event);
            }else {
                throw new RuntimeException("Quantite insuffisante");
            }
        }
        if (event.getStatus().equalsIgnoreCase(EventStatus.CANCELED.name())) {
            ProductModel product=productService.findProductById(event.getProductEventDto().getProductIdEvent());
            int qtyUsed=event.getProductEventDto().getQty();
            int qr=productService.qtyRestante(product.getQty(),qtyUsed,event.getStatus());

                productService.updateProductQty(event.getProductEventDto().getProductIdEvent(),qr);

        }
        LOGGER.info("Product Updated event received in Inventory service => {}", event);
    }
}

