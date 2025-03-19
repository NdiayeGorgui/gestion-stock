package com.gogo.delivered_query_service.kafka;

import com.gogo.base_domaine_service.event.CustomerEventDto;
import com.gogo.base_domaine_service.event.EventStatus;
import com.gogo.base_domaine_service.event.OrderEventDto;
import com.gogo.delivered_query_service.model.Delivered;
import com.gogo.delivered_query_service.service.DeliveredQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;


@Service
public class DeliveredCommandConsumer {

    @Autowired
    private DeliveredQueryService deliveredQueryService;
 


    private static final Logger LOGGER = LoggerFactory.getLogger(DeliveredCommandConsumer.class);

    @KafkaListener(
            topics = "${spring.kafka.topic.delivered.name}"
            , groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumeProductStatus(OrderEventDto event) {

    	//save the event sourcing table with shipped status
        if (event.getStatus().equalsIgnoreCase(EventStatus.DELIVERING.name())) {
        	List<Delivered> deliveredList=deliveredQueryService.findByPaymentIdAndOrderIdAndStatus(event.getPaymentId(),event.getId(),EventStatus.DELIVERING.name());
              if(deliveredList.isEmpty()) {
            	  Delivered delivered=new Delivered();
                  delivered.setOrderId(event.getId());
                  delivered.setPaymentId(event.getPaymentId());
                  delivered.setCustomerId(event.getCustomerEventDto().getCustomerIdEvent());
                  delivered.setCustomerName(event.getCustomerEventDto().getName());
                  delivered.setCustomerMail(event.getCustomerEventDto().getEmail());
                  delivered.setStatus(EventStatus.DELIVERING.name());
                  delivered.setEventTimeStamp(LocalDateTime.now());
                  delivered.setDetails("Order is in delivering status");
                  deliveredQueryService.saveDeliveredQuery(delivered);
              }
           // Vérifier si cette commande est déjà  délivrée
              boolean isAlreadyProcessed = deliveredQueryService.isOrderAlreadyProcessed(event.getPaymentId(),event.getId());
              for (Delivered orderDelivered:deliveredList){
            	
                  if (!isAlreadyProcessed) {
                	  orderDelivered.setOrderId(event.getId());
                	  orderDelivered.setPaymentId(event.getPaymentId());
                	  orderDelivered.setCustomerId(event.getCustomerEventDto().getCustomerIdEvent());
                	  orderDelivered.setCustomerName(event.getCustomerEventDto().getName());
                	  orderDelivered.setCustomerMail(event.getCustomerEventDto().getEmail());
                	  orderDelivered.setStatus(EventStatus.DELIVERING.name());
                	  orderDelivered.setEventTimeStamp(LocalDateTime.now());
                	  orderDelivered.setDetails("Order is in delivering status");
                	  deliveredQueryService.saveDeliveredQuery(orderDelivered);
                  }
            	 
              }
               

            LOGGER.info("Oder event received in Delivered command service with delivering status => {}", event);

        }
    

        if (event.getStatus().equalsIgnoreCase(EventStatus.DELIVERED.name())) {

        	Delivered existingDelivered=deliveredQueryService.findByCustomerIdAndOrderIdAndStatus(event.getCustomerEventDto().getCustomerIdEvent(),event.getId(), EventStatus.DELIVERING.name());
    		OrderEventDto orderEventDto=new OrderEventDto();
            CustomerEventDto customerEventDto=new CustomerEventDto();

            existingDelivered.setStatus(EventStatus.DELIVERED.name());
            existingDelivered.setDetails("Order is delivered");
            deliveredQueryService.saveDeliveredQuery(existingDelivered);

            customerEventDto.setCustomerIdEvent(existingDelivered.getCustomerId());
            customerEventDto.setName(existingDelivered.getCustomerName());
            customerEventDto.setEmail(existingDelivered.getCustomerMail());

            orderEventDto.setStatus(EventStatus.DELIVERED.name());
            orderEventDto.setId(existingDelivered.getOrderId());
            orderEventDto.setPaymentId(existingDelivered.getPaymentId());
            orderEventDto.setCustomerEventDto(customerEventDto);

            LOGGER.info("Oder event received in Delivered command service with delivering status => {}", event);

        }
    }
}

