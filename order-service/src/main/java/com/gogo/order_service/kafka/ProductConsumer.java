package com.gogo.order_service.kafka;

import com.gogo.base_domaine_service.event.EventStatus;
import com.gogo.base_domaine_service.event.OrderEventDto;
import com.gogo.base_domaine_service.event.ProductEvent;
import com.gogo.base_domaine_service.event.ProductEventDto;
import com.gogo.order_service.mapper.OrderMapper;
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
        OrderEventDto orderEventDto = new OrderEventDto();
        if (event.getStatus().equalsIgnoreCase(EventStatus.PENDING.name())) {
            Product product = OrderMapper.mapToProductModel(event);
            orderService.saveProduit(product);

            boolean productExist = productRepository.existsByProductIdEventAndStatus(event.getProduct().getProductIdEvent(), EventStatus.CREATED.name());
            ProductEventDto productEventDto=OrderMapper.mapToProductEventDto(event);
            if (productExist) {
                //update product with created
                orderEventDto.setStatus(EventStatus.CREATED.name());
                orderEventDto.setProductEventDto(productEventDto);
                // event.setMessage("Product status is in created state");
                LOGGER.info("Product Update event with created status sent to Inventory service => {}", orderEventDto);
                // updateKafkaTemplate.send(UPDATE_CUSTOMER_EVENT,updateCustomerEvent(event,STATUS));
            } else {
                //update customer with failed
                orderEventDto.setStatus(EventStatus.FAILED.name());
                orderEventDto.setProductEventDto(productEventDto);
                // event.setMessage("Product status is in failed state");
                LOGGER.info("Product Update event with failed status sent to Customer service => {}", orderEventDto);
            }
            productProducer.sendMessage(orderEventDto);
        }
        if (event.getStatus().equalsIgnoreCase(EventStatus.DELETING.name())) {

            boolean productExist = productRepository.existsByProductIdEventAndStatus(event.getProduct().getProductIdEvent(), EventStatus.CREATED.name());
            if (productExist) {
                Product product = productRepository.findProductByProductIdEvent(event.getProduct().getProductIdEvent());
                productRepository.deleteProduct(product.getProductIdEvent());
                //verifying if exists customer object
                boolean productDeletedExist = productRepository.existsByProductIdEventAndStatus(event.getProduct().getProductIdEvent(), EventStatus.CREATED.name());
                if (!productDeletedExist) {
                    ProductEventDto productEventDto=OrderMapper.mapToProductEventDto(event);

                    orderEventDto.setStatus(EventStatus.DELETED.name());
                    orderEventDto.setProductEventDto(productEventDto);
                    LOGGER.info("Product Update event with deleted status sent to Inventory service => {}", orderEventDto);
                    productProducer.sendMessage(orderEventDto);
                }
            }
        }
        if (event.getStatus().equalsIgnoreCase(EventStatus.UPDATING.name())) {

            boolean productExist = productRepository.existsByProductIdEventAndStatus(event.getProduct().getProductIdEvent(), EventStatus.CREATED.name());
            if (productExist) {
                productRepository.updateProduct(event.getProduct().getProductIdEvent(), EventStatus.CREATED.name(), event.getProduct().getName(),event.getProduct().getCategory(),event.getProduct().getDescription(),event.getProduct().getLocation(), event.getProduct().getQty(), event.getProduct().getPrice(),event.getProduct().getQtyStatus());
                ProductEventDto productEventDto=OrderMapper.mapToProductEventDto(event);

                orderEventDto.setStatus(EventStatus.UPDATED.name());
                orderEventDto.setProductEventDto(productEventDto);
                event.setMessage("Product status is in updated state");
                LOGGER.info("Product Update event with updated status sent to Inventory service => {}", orderEventDto);
                productProducer.sendMessage(orderEventDto);
            }
        }
        if(event.getStatus().equalsIgnoreCase(EventStatus.UNAVAILABLE.name())){
            Product product=orderService.findProductById(event.getProduct().getProductIdEvent());
            if(product.getQty()==0){
                product.setQtyStatus(EventStatus.UNAVAILABLE.name());
                productRepository.updateProductQtyStatus(product.getProductIdEvent(),product.getQtyStatus());
            }
        }
    }
}
