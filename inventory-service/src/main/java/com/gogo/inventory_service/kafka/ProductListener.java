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

        if(orderEventDto.getStatus().equalsIgnoreCase("CREATED")){

            productService.updateProductStatus(orderEventDto.getId(), orderEventDto.getStatus());
        }

        if(orderEventDto.getStatus().equalsIgnoreCase("DELETED")){
            productService.deleteProduct(orderEventDto.getId(), orderEventDto.getStatus());
        }
        if(orderEventDto.getStatus().equalsIgnoreCase("UPDATED")){
            productService.updateProduct(orderEventDto.getId(),"CREATED", orderEventDto.getName(), orderEventDto.getProductEventDto().getQty(), orderEventDto.getProductEventDto().getPrice(),orderEventDto.getProductEventDto().getQtyStatus());
        }

        LOGGER.info(String.format("Product Updated event received in Inventory service => %s", orderEventDto));


    }
}
