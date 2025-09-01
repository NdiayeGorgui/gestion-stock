package com.gogo.mcp_java_server.kafka;

import com.gogo.base_domaine_service.event.CustomerEvent;
import com.gogo.base_domaine_service.event.EventStatus;
import com.gogo.base_domaine_service.event.OrderEventDto;
import com.gogo.base_domaine_service.event.CustomerEventDto;

import com.gogo.mcp_java_server.mapper.MpcMapper;
import com.gogo.mcp_java_server.model.Customer;
import com.gogo.mcp_java_server.repository.CustomerRepository;
import com.gogo.mcp_java_server.service.StockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class CustomerConsumer {
    @Autowired
    private StockService stockService;
    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    private KafkaTemplate<String, CustomerEventDto> updateKafkaTemplate;
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomerConsumer.class);

    @KafkaListener(
            topics = "${spring.kafka.topic.customer.name}"
            , groupId = "${spring.kafka.consumer.customer.group-id}"
    )
    public void consumeCustomer(CustomerEvent event) {

        // save the customer event into the database
        if (event.getStatus().equalsIgnoreCase(EventStatus.PENDING.name())) {
            LOGGER.info("Customer event received in Mpc service => {}", event);
            Customer customer = MpcMapper.mapToCustomerEvent(event);
            stockService.saveCustomer(customer);

        }

        if (event.getStatus().equalsIgnoreCase(EventStatus.DELETING.name())) {

            boolean customerExist = customerRepository.existsByCustomerIdAndStatus(event.getCustomer().getCustomerIdEvent(), EventStatus.CREATED.name());
            if (customerExist) {
                Customer existingCustomer = customerRepository.findCustomerByCustomerId(event.getCustomer().getCustomerIdEvent());
                customerRepository.deleteCustomer(existingCustomer.getCustomerId());

            }
        }
        if (event.getStatus().equalsIgnoreCase(EventStatus.UPDATING.name())) {

            boolean customerExist = customerRepository.existsByCustomerIdAndStatus(event.getCustomer().getCustomerIdEvent(), EventStatus.CREATED.name());
            if (customerExist) {

                customerRepository.updateCustomer(event.getCustomer().getCustomerIdEvent(), EventStatus.CREATED.name(), event.getCustomer().getName(), event.getCustomer().getPhone(), event.getCustomer().getEmail(), event.getCustomer().getAddress(),event.getCustomer().getCity(), event.getCustomer().getPostalCode());


            }
        }
    }
}
