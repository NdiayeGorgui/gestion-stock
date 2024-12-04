package com.gogo.order_service.kafka;

import com.gogo.base_domaine_service.event.ProductEvent;
import com.gogo.base_domaine_service.event.OrderEventDto;
import com.gogo.base_domaine_service.event.ProductEventDto;
import com.gogo.order_service.model.Product;
import com.gogo.order_service.repository.ProductRepository;
import com.gogo.order_service.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class ProductConsumer {
    @Autowired
    private OrderService orderService;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductProducer productProducer;
    private static final Logger LOGGER = LoggerFactory.getLogger(ProductConsumer.class);

    @KafkaListener(
            topics = "${spring.kafka.topic.product.name}"
            , groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumeOrder(ProductEvent event) {


        String STATUS = "CREATED";
        if (event.getStatus().equalsIgnoreCase("PENDING")) {
            Product produit = new Product();
            produit.setProductIdEvent(event.getProduct().getId());
            produit.setName(event.getProduct().getName());
            produit.setQty(event.getProduct().getQty());
            produit.setPrice(event.getProduct().getPrice());
            produit.setStatus(STATUS);
            produit.setQtyStatus("AVAILABLE");
            orderService.saveProduit(produit);

            OrderEventDto orderEventDto = new OrderEventDto();

            boolean productExist = productRepository.existsByProductIdEventAndStatus(event.getProduct().getId(), STATUS);
            if (productExist) {
                //update product with created
                orderEventDto.setStatus(STATUS);
                orderEventDto.setId(event.getProduct().getId());

                // event.setMessage("Product status is in created state");
                LOGGER.info(String.format("Product Update event with created status sent to Inventory service => %s", orderEventDto));
                productProducer.sendMessage(orderEventDto);
                // updateKafkaTemplate.send(UPDATE_CUSTOMER_EVENT,updateCustomerEvent(event,STATUS));
            } else {
                //update customer with failed
                orderEventDto.setStatus("FAILED");
                orderEventDto.setId(event.getProduct().getId());
                // event.setMessage("Product status is in failed state");
                LOGGER.info(String.format("Product Update event with failed status sent to Customer service => %s", orderEventDto));
                productProducer.sendMessage(orderEventDto);
            }
        }
        if (event.getStatus().equalsIgnoreCase("DELETING")) {
            OrderEventDto orderEventDto = new OrderEventDto();
            boolean productExist = productRepository.existsByProductIdEventAndStatus(event.getProduct().getId(), "CREATED");
            if (productExist) {
                Product product = productRepository.findProductByProductIdEvent(event.getProduct().getId());
                productRepository.deleteProduct(product.getProductIdEvent());
                //verifying if exists customer object
                boolean productDeletedExist = productRepository.existsByProductIdEventAndStatus(event.getProduct().getId(), "CREATED");
                if (!productDeletedExist) {
                    orderEventDto.setStatus("DELETED");
                    orderEventDto.setId(event.getProduct().getId());
                    orderEventDto.setName(event.getProduct().getName());

                    ProductEventDto productEventDto = new ProductEventDto();
                    productEventDto.setPrice(event.getProduct().getPrice());
                    productEventDto.setQty(event.getProduct().getQty());

                    orderEventDto.setProductEventDto(productEventDto);
                    LOGGER.info(String.format("Product Update event with deleted status sent to Inventory service => %s", orderEventDto));
                    productProducer.sendMessage(orderEventDto);
                }
            }
        }
        if (event.getStatus().equalsIgnoreCase("UPDATING")) {
            OrderEventDto orderEventDto = new OrderEventDto();
            boolean productExist = productRepository.existsByProductIdEventAndStatus(event.getProduct().getId(), "CREATED");
            if (productExist) {
                productRepository.updateProduct(event.getProduct().getId(), "CREATED", event.getProduct().getName(), event.getProduct().getQty(), event.getProduct().getPrice());
                orderEventDto.setStatus("UPDATED");
                orderEventDto.setId(event.getProduct().getId());
                orderEventDto.setName(event.getProduct().getName());

                ProductEventDto productEventDto = new ProductEventDto();
                productEventDto.setPrice(event.getProduct().getPrice());
                productEventDto.setQty(event.getProduct().getQty());

                orderEventDto.setProductEventDto(productEventDto);
                event.setMessage("Product status is in updated state");
                LOGGER.info(String.format("Product Update event with updated status sent to Inventory service => %s", orderEventDto));
                productProducer.sendMessage(orderEventDto);
            }
        }
    }
}
