package com.gogo.mcp_java_server.kafka;

import com.gogo.base_domaine_service.event.EventStatus;
import com.gogo.base_domaine_service.event.ProductEvent;

import com.gogo.mcp_java_server.mapper.MpcMapper;
import com.gogo.mcp_java_server.model.Product;
import com.gogo.mcp_java_server.repository.ProductRepository;
import com.gogo.mcp_java_server.service.StockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class ProductConsumer {
    @Autowired
    private StockService stockService;
    @Autowired
    private ProductRepository productRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductConsumer.class);

    @KafkaListener(
            topics = "${spring.kafka.topic.product.name}"
            , groupId = "${spring.kafka.consumer.product.group-id}"
    )
    public void consumeOrder(ProductEvent event) {

        if (event.getStatus().equalsIgnoreCase(EventStatus.PENDING.name())) {
            Product product = MpcMapper.mapToProductEvent(event);
            stockService.saveProduct(product);

        }
        if (event.getStatus().equalsIgnoreCase(EventStatus.DELETING.name())) {
            boolean productExist = productRepository.existsByProductIdAndStatus(event.getProduct().getProductIdEvent(), EventStatus.CREATED.name());
            if (productExist) {
                Product product = productRepository.findProductByProductId(event.getProduct().getProductIdEvent());
                productRepository.deleteProduct(product.getProductId());
            }
        }
        if (event.getStatus().equalsIgnoreCase(EventStatus.UPDATING.name())) {

            boolean productExist = productRepository.existsByProductIdAndStatus(event.getProduct().getProductIdEvent(), EventStatus.CREATED.name());
            if (productExist) {
                productRepository.updateProduct(event.getProduct().getProductIdEvent(), EventStatus.CREATED.name(), event.getProduct().getName(),event.getProduct().getCategory(),event.getProduct().getDescription(),event.getProduct().getLocation(), event.getProduct().getQty(), event.getProduct().getPrice(),event.getProduct().getQtyStatus());
            }
        }
    }
}
