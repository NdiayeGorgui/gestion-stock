package com.gogo.customer_service.kafka;

import com.gogo.base_domaine_service.event.OrderEventDto;
import com.gogo.customer_service.service.CustomerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class CustomerListener {

    @Autowired
    private CustomerService customerService;
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomerListener.class);

    @KafkaListener(
            topics = "${spring.kafka.topic.customer.update.name}"
            ,groupId = "${spring.kafka.update.customer.consumer.group-id}"
    )
    public void consumeCustomerStatus(OrderEventDto orderEventDto){
        if(orderEventDto.getStatus().equalsIgnoreCase("CREATED")){
            customerService.updateCustomerStatus(orderEventDto.getId(), orderEventDto.getStatus());
        }
        if(orderEventDto.getStatus().equalsIgnoreCase("DELETED")){
            customerService.deleteCustomer(orderEventDto.getId(), orderEventDto.getStatus());
        }
        if (orderEventDto.getStatus().equalsIgnoreCase("UPDATED")) {
            customerService.updateCustomer(orderEventDto.getId(), "CREATED", orderEventDto.getName(), orderEventDto.getCustomerEventDto().getPhone(), orderEventDto.getCustomerEventDto().getEmail(), orderEventDto.getCustomerEventDto().getAddress());
        }

        LOGGER.info(String.format("Customer Updated event received in Customer service => %s", orderEventDto));

    }
}
