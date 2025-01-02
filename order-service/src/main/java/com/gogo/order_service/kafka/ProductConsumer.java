package com.gogo.order_service.kafka;

import com.gogo.base_domaine_service.event.EventStatus;
import com.gogo.base_domaine_service.event.ProductEvent;
import com.gogo.base_domaine_service.event.OrderEventDto;
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

            boolean productExist = productRepository.existsByProductIdEventAndStatus(event.getProduct().getId(), EventStatus.CREATED.name());
            if (productExist) {
                //update product with created
                ProductEventDto productEventDto=new ProductEventDto();
                orderEventDto.setStatus(EventStatus.CREATED.name());

                productEventDto.setProductIdEvent(event.getProduct().getId());

                orderEventDto.setProductEventDto(productEventDto);

                // event.setMessage("Product status is in created state");
                LOGGER.info("Product Update event with created status sent to Inventory service => {}", orderEventDto);
                productProducer.sendMessage(orderEventDto);
                // updateKafkaTemplate.send(UPDATE_CUSTOMER_EVENT,updateCustomerEvent(event,STATUS));
            } else {
                //update customer with failed
                orderEventDto.setStatus(EventStatus.FAILED.name());

                ProductEventDto productEventDto=new ProductEventDto();
                productEventDto.setProductIdEvent(event.getProduct().getId());
                orderEventDto.setProductEventDto(productEventDto);

                // event.setMessage("Product status is in failed state");
                LOGGER.info("Product Update event with failed status sent to Customer service => {}", orderEventDto);
                productProducer.sendMessage(orderEventDto);
            }
        }
        if (event.getStatus().equalsIgnoreCase(EventStatus.DELETING.name())) {

            boolean productExist = productRepository.existsByProductIdEventAndStatus(event.getProduct().getId(), EventStatus.CREATED.name());
            if (productExist) {
                Product product = productRepository.findProductByProductIdEvent(event.getProduct().getId());
                productRepository.deleteProduct(product.getProductIdEvent());
                //verifying if exists customer object
                boolean productDeletedExist = productRepository.existsByProductIdEventAndStatus(event.getProduct().getId(), EventStatus.CREATED.name());
                if (!productDeletedExist) {
                    orderEventDto.setStatus(EventStatus.DELETED.name());
                    orderEventDto.setName(event.getProduct().getName());

                    ProductEventDto productEventDto=new ProductEventDto();
                    productEventDto.setProductIdEvent(event.getProduct().getId());

                    orderEventDto.setProductEventDto(productEventDto);
                    LOGGER.info("Product Update event with deleted status sent to Inventory service => {}", orderEventDto);
                    productProducer.sendMessage(orderEventDto);
                }
            }
        }
        if (event.getStatus().equalsIgnoreCase(EventStatus.UPDATING.name())) {

            boolean productExist = productRepository.existsByProductIdEventAndStatus(event.getProduct().getId(), EventStatus.CREATED.name());
            if (productExist) {
                productRepository.updateProduct(event.getProduct().getId(), EventStatus.CREATED.name(), event.getProduct().getName(), event.getProduct().getQty(), event.getProduct().getPrice(),event.getProduct().getQtyStatus());

                orderEventDto.setStatus(EventStatus.UPDATED.name());

                ProductEventDto productEventDto = new ProductEventDto();

                productEventDto.setProductIdEvent(event.getProduct().getId());
                productEventDto.setName(event.getProduct().getName());
                productEventDto.setPrice(event.getProduct().getPrice());
                productEventDto.setQty(event.getProduct().getQty());
                productEventDto.setQtyStatus(event.getProduct().getQtyStatus());

                orderEventDto.setProductEventDto(productEventDto);
                event.setMessage("Product status is in updated state");
                LOGGER.info("Product Update event with updated status sent to Inventory service => {}", orderEventDto);
                productProducer.sendMessage(orderEventDto);
            }
        }
        if(event.getStatus().equalsIgnoreCase(EventStatus.UNAVAILABLE.name())){
            Product product=orderService.findProductById(event.getProduct().getId());
            if(product.getQty()==0){
                product.setQtyStatus(EventStatus.UNAVAILABLE.name());
                productRepository.updateProductQtyStatus(product.getProductIdEvent(),product.getQtyStatus());
            }
        }
    }
}
