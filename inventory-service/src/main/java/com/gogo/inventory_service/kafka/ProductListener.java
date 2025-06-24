package com.gogo.inventory_service.kafka;

import com.gogo.base_domaine_service.event.*;
import com.gogo.inventory_service.model.ProductModel;
import com.gogo.inventory_service.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class ProductListener {

    @Autowired
    private ProductService productService;

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductListener.class);

    @KafkaListener(
            topics = "${spring.kafka.topic.product.update.name}"
            ,groupId = "${spring.kafka.update.product.consumer.group-id}"
    )
    public void consumeProductStatus(OrderEventDto orderEventDto){
        if (orderEventDto.getProductEventDto() != null) {
            if(orderEventDto.getStatus().equalsIgnoreCase(EventStatus.CREATED.name())){
                productService.updateProductStatus(orderEventDto.getProductEventDto().getProductIdEvent(), orderEventDto.getStatus());
            }
            if(orderEventDto.getStatus().equalsIgnoreCase(EventStatus.DELETED.name())){
                productService.deleteProduct(orderEventDto.getProductEventDto().getProductIdEvent(), orderEventDto.getStatus());
            }
            if(orderEventDto.getStatus().equalsIgnoreCase(EventStatus.UPDATED.name())){
                productService.updateProduct(orderEventDto.getProductEventDto().getProductIdEvent(),EventStatus.CREATED.name(), orderEventDto.getProductEventDto().getName(),orderEventDto.getProductEventDto().getCategory(),orderEventDto.getProductEventDto().getDescription(),orderEventDto.getProductEventDto().getLocation(), orderEventDto.getProductEventDto().getQty(), orderEventDto.getProductEventDto().getPrice(),orderEventDto.getProductEventDto().getQtyStatus());
            }
            if(orderEventDto.getStatus().equalsIgnoreCase(EventStatus.FAILED.name())){
                productService.updateProduct(orderEventDto.getProductEventDto().getProductIdEvent(),EventStatus.FAILED.name(), orderEventDto.getProductEventDto().getName(),orderEventDto.getProductEventDto().getCategory(),orderEventDto.getProductEventDto().getDescription(),orderEventDto.getProductEventDto().getLocation(), orderEventDto.getProductEventDto().getQty(), orderEventDto.getProductEventDto().getPrice(),orderEventDto.getProductEventDto().getQtyStatus());
            }
        }

        LOGGER.info("Product Updated event received in Inventory service => {}", orderEventDto);
    }
}
