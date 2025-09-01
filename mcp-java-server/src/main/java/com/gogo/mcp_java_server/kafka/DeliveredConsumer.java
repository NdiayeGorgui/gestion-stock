package com.gogo.mcp_java_server.kafka;


import com.gogo.base_domaine_service.event.EventStatus;
import com.gogo.base_domaine_service.event.OrderEventDto;
import com.gogo.mcp_java_server.model.Delivered;
import com.gogo.mcp_java_server.repository.DeliveredRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class DeliveredConsumer {

    @Autowired
    private DeliveredRepository deliveredRepository;


    private static final Logger LOGGER = LoggerFactory.getLogger(DeliveredConsumer.class);

    @KafkaListener(
            topics = "${spring.kafka.topic.delivered.name}"
            , groupId = "${spring.kafka.consumer.delivered.group-id}"
    )
    public void consumeDeliver(OrderEventDto event) {

        //save the delivered table with delivered status
        if (event.getStatus().equalsIgnoreCase(EventStatus.DELIVERED.name())) {

            Delivered delivered=new Delivered();
            delivered.setOrderId(event.getId());
           // delivered.setPaymentId(event.getPaymentId());
            delivered.setCustomerId(event.getCustomerEventDto().getCustomerIdEvent());
            delivered.setCustomerName(event.getCustomerEventDto().getName());
            delivered.setCustomerMail(event.getCustomerEventDto().getEmail());
            delivered.setDetails("Order Delivered");
            delivered.setStatus(event.getStatus());
            deliveredRepository.save(delivered);

            LOGGER.info("Oder event received in mpc-server  with delivered status => {}", event);

        }
    }
}


