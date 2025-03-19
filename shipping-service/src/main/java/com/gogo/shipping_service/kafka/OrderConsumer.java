package com.gogo.shipping_service.kafka;

import com.gogo.base_domaine_service.event.EventStatus;
import com.gogo.base_domaine_service.event.OrderEventDto;
import com.gogo.shipping_service.mapper.ShippingMapper;
import com.gogo.shipping_service.model.Ship;
import com.gogo.shipping_service.service.ShippingService;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class OrderConsumer {
    @Autowired
    private ShippingService shippingService;


    private static final Logger LOGGER = LoggerFactory.getLogger(OrderConsumer.class);

    @KafkaListener(
            topics = "${spring.kafka.topic.name}"
            ,groupId = "${spring.kafka.consumer.group-id}"
    )
    public void orderConsumer(OrderEventDto event){
        if(event.getStatus().equalsIgnoreCase(EventStatus.CONFIRMED.name())){
        	List<Ship> shippedList=shippingService.findByPaymentAndStatus(event.getPaymentId(),event.getId(),EventStatus.SHIPPING.name());
        	 if(shippedList.isEmpty()) {
        		 Ship ship= ShippingMapper.mapToShip(event);
                 shippingService.saveShip(ship);
        	 }
        	// Vérifier si cette commande est déjà  délivrée
             boolean isAlreadyProcessed = shippingService.isOrderAlreadyProcessed(event.getPaymentId(),event.getId());
             for (Ship orderShipped:shippedList){
            	 if (!isAlreadyProcessed) {
            		 orderShipped.setOrderId(event.getId());
            		 orderShipped.setPaymentId(event.getPaymentId());
            		 orderShipped.setCustomerId(event.getCustomerEventDto().getCustomerIdEvent());
            		 orderShipped.setCustomerName(event.getCustomerEventDto().getName());
            		 orderShipped.setCustomerMail(event.getCustomerEventDto().getEmail());
            		 orderShipped.setStatus(EventStatus.SHIPPING.name());
            		 orderShipped.setDetails("Order is in shipping status");
            		 orderShipped.setEventTimeStamp(LocalDateTime.now());
            		 
            		
                     shippingService.saveShip(orderShipped);
                 }

             }
           	
                
            

           // event.setStatus(EventStatus.SHIPPED.name());
           // shippingProducer.sendMessage(event);
            LOGGER.info("Order event received in shipping service => {}", event);
        }
    }
}
