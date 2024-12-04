/*

package com.gogo.order_service.kafka;

import com.gogo.base_domaine_service.event.OrderEventDto;
import com.gogo.base_domaine_service.event.ProductEventDto;
import com.gogo.order_service.model.Product;
import com.gogo.order_service.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;

public class OrderConsumer {
    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderProducer orderProducer;
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderConsumer.class);

    @KafkaListener(
            topics = "${spring.kafka.topic.billing.name}"
            ,groupId = "${spring.kafka.update.bill.consumer.group-id}"
    )
    public void consumeProductStatus(OrderEventDto event){

        if (event.getStatus().equalsIgnoreCase("MODIFIED")) {
            Product product=orderService.findProductById(event.getProductEventDto().getProductIdEvent());
            int qtyUsed= event.getProductItemEventDto().getQty();
            int qr=orderService.qtyRestante(product.getQty(),qtyUsed);
            if(qr>0){
                orderService.updateQuantity(event.getProductEventDto().getProductIdEvent(),qr);
                orderService.updateOrderStatus(event.getId(),"CREATED");
               // event.setStatus("MODIFIED");
               // event.setId(event.getProductEventDto().getProductIdEvent());
              //  event.setName(event.getProductEventDto().getName());

               // ProductEventDto productEventDto = new ProductEventDto();
              //  productEventDto.setPrice(event.getProductEventDto().getPrice());
               // productEventDto.setQty(event.getProductEventDto().getQty());

               // event.setProductEventDto(productEventDto);
                LOGGER.info(String.format("Product Update event with updated quantity status received to order service => %s", event));
                //  orderProducer.sendMessage(event);
            }else {
                throw new RuntimeException("Quantite insuffisant");
            }


        }

        LOGGER.info(String.format("Product Updated event received in Inventory service => %s", event));



    }
}

*/
