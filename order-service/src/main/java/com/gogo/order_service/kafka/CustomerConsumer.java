package com.gogo.order_service.kafka;

import com.gogo.base_domaine_service.event.CustomerEvent;
import com.gogo.base_domaine_service.event.EventStatus;
import com.gogo.base_domaine_service.event.OrderEventDto;
import com.gogo.base_domaine_service.event.CustomerEventDto;
import com.gogo.order_service.mapper.OrderMapper;
import com.gogo.order_service.model.Customer;
import com.gogo.order_service.repository.CustomerRepository;
import com.gogo.order_service.repository.OrderRepository;
import com.gogo.order_service.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class CustomerConsumer {
    @Autowired
    private OrderService orderService;
    @Autowired
    CustomerRepository customerRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private CustomerProducer customerProducer;
    @Autowired
    private KafkaTemplate<String, CustomerEventDto> updateKafkaTemplate;
    private final String UPDATE_CUSTOMER_EVENT = "update_customer_event";
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomerConsumer.class);

    @KafkaListener(
            topics = "${spring.kafka.topic.customer.name}"
            , groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumeOrder(CustomerEvent event) {
        OrderEventDto orderEventDto = new OrderEventDto();
        // save the customer event into the database
        if (event.getStatus().equalsIgnoreCase(EventStatus.PENDING.name())) {
            LOGGER.info("Customer event received in Order service => {}", event);
            Customer customer = OrderMapper.mapToCustomerModel(event);
            orderService.saveClient(customer);

            boolean customerExist = customerRepository.existsByCustomerIdEventAndStatus(event.getCustomer().getId(), EventStatus.CREATED.name());

            if (customerExist) {
                //update customer with created
                orderEventDto.setStatus(EventStatus.CREATED.name());
                CustomerEventDto customerEventDto=new CustomerEventDto();
                customerEventDto.setCustomerIdEvent(event.getCustomer().getId());
                orderEventDto.setCustomerEventDto(customerEventDto);
                // event.setMessage("customer status is in created state");
                LOGGER.info("Customer Update event with created status sent to Customer service => {}", orderEventDto);
                customerProducer.sendMessage(orderEventDto);
            } else {
                //update customer with failed
                orderEventDto.setStatus(EventStatus.FAILED.name());
                CustomerEventDto customerEventDto=new CustomerEventDto();
                customerEventDto.setCustomerIdEvent(event.getCustomer().getId());
                orderEventDto.setCustomerEventDto(customerEventDto);
                event.setMessage("customer status is in failed state");
                LOGGER.info("Customer Update event with failed status sent to Customer service => {}", orderEventDto);
                customerProducer.sendMessage(orderEventDto);
            }
        }
        if (event.getStatus().equalsIgnoreCase(EventStatus.DELETING.name())) {

            boolean customerExist = customerRepository.existsByCustomerIdEventAndStatus(event.getCustomer().getId(), EventStatus.CREATED.name());
            if (customerExist) {
                Customer customer= customerRepository.findCustomerByCustomerIdEvent(event.getCustomer().getId());
                customerRepository.deleteCustomer(customer.getCustomerIdEvent());
                //verifying if exists customer object
                boolean customerDeletedExist = customerRepository.existsByCustomerIdEventAndStatus(event.getCustomer().getId(), EventStatus.CREATED.name());
                if(!customerDeletedExist){
                    orderEventDto.setStatus(EventStatus.DELETED.name());
                    CustomerEventDto customerEventDto=new CustomerEventDto();
                    customerEventDto.setCustomerIdEvent(event.getCustomer().getId());
                    orderEventDto.setCustomerEventDto(customerEventDto);
                    // event.setMessage("customer status is in created state");
                    LOGGER.info("Customer Update event with deleted status sent to Customer service => {}", orderEventDto);
                    customerProducer.sendMessage(orderEventDto);
                }
            }
        }
        if (event.getStatus().equalsIgnoreCase(EventStatus.UPDATING.name())) {

            boolean customerExist = customerRepository.existsByCustomerIdEventAndStatus(event.getCustomer().getId(), EventStatus.CREATED.name());
            if (customerExist) {
                Customer customer = customerRepository.findCustomerByCustomerIdEvent(event.getCustomer().getId());
                customerRepository.updateCustomer(event.getCustomer().getId(),EventStatus.CREATED.name(),event.getCustomer().getName(),event.getCustomer().getPhone(),event.getCustomer().getEmail(),event.getCustomer().getAddress());
                orderEventDto.setStatus(EventStatus.UPDATED.name());

                CustomerEventDto customerEventDto =new CustomerEventDto();

                customerEventDto.setCustomerIdEvent(event.getCustomer().getId());
                customerEventDto.setName(event.getCustomer().getName());
                customerEventDto.setEmail(event.getCustomer().getEmail());
                customerEventDto.setPhone(event.getCustomer().getPhone());
                customerEventDto.setAddress(event.getCustomer().getAddress());
                orderEventDto.setCustomerEventDto(customerEventDto);
                event.setMessage("customer status is in updated state");
                LOGGER.info("Customer Update event with updated status sent to Customer service => {}", orderEventDto);
                customerProducer.sendMessage(orderEventDto);
            }
        }
    }
}
