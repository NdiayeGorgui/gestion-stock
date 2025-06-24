package com.gogo.customer_service.kafka;

import com.gogo.base_domaine_service.event.EventStatus;
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
    public void consumeCustomerStatus(OrderEventDto orderEventDto) {
        if (orderEventDto.getCustomerEventDto() != null) {
            if (orderEventDto.getStatus().equalsIgnoreCase(EventStatus.CREATED.name())) {
                customerService.updateCustomerStatus(orderEventDto.getCustomerEventDto().getCustomerIdEvent(), orderEventDto.getStatus());
            }
            if (orderEventDto.getStatus().equalsIgnoreCase(EventStatus.DELETED.name())) {
                customerService.deleteCustomer(orderEventDto.getCustomerEventDto().getCustomerIdEvent(), orderEventDto.getStatus());
            }
            if (orderEventDto.getStatus().equalsIgnoreCase(EventStatus.UPDATED.name())) {

                customerService.updateCustomer(orderEventDto.getCustomerEventDto().getCustomerIdEvent(), EventStatus.CREATED.name(), orderEventDto.getCustomerEventDto().getName(), orderEventDto.getCustomerEventDto().getPhone(), orderEventDto.getCustomerEventDto().getEmail(), orderEventDto.getCustomerEventDto().getAddress(),orderEventDto.getCustomerEventDto().getCity(),orderEventDto.getCustomerEventDto().getPostalCode());
            }
            if (orderEventDto.getStatus().equalsIgnoreCase(EventStatus.FAILED.name())) {

                customerService.updateCustomer(orderEventDto.getCustomerEventDto().getCustomerIdEvent(), EventStatus.FAILED.name(), orderEventDto.getCustomerEventDto().getName(), orderEventDto.getCustomerEventDto().getPhone(), orderEventDto.getCustomerEventDto().getEmail(), orderEventDto.getCustomerEventDto().getAddress(),orderEventDto.getCustomerEventDto().getCity(),orderEventDto.getCustomerEventDto().getPostalCode());
            }
        }

        LOGGER.info("Customer Updated event received in Customer service => {}", orderEventDto);
    }
}
