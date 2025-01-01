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
        // save the customer event into the database
        if (event.getStatus().equalsIgnoreCase(EventStatus.PENDING.name())) {
            LOGGER.info("Customer event received in Order service => {}", event);
            Customer customer = OrderMapper.mapToCustomerModel(event);
            orderService.saveClient(customer);

            OrderEventDto orderEventDto = new OrderEventDto();

            boolean customerExist = customerRepository.existsByCustomerIdEventAndStatus(event.getCustomer().getId(), EventStatus.CREATED.name());

            if (customerExist) {
                //update customer with created
                // updateCustomerEvent(event,STATUS);

                orderEventDto.setStatus(EventStatus.CREATED.name());
                orderEventDto.setId(event.getCustomer().getId());
                // event.setStatus(STATUS);
                // event.setMessage("customer status is in created state");
                LOGGER.info("Customer Update event with created status sent to Customer service => {}", orderEventDto);
                customerProducer.sendMessage(orderEventDto);
                // updateKafkaTemplate.send(UPDATE_CUSTOMER_EVENT,updateCustomerEvent(event,STATUS));
            } else {
                //update customer with failed
                // updateCustomerEvent(event,"FAILED");
                orderEventDto.setStatus(EventStatus.FAILED.name());
                orderEventDto.setId(event.getCustomer().getId());
                event.setMessage("customer status is in failed state");
                LOGGER.info("Customer Update event with failed status sent to Customer service => {}", orderEventDto);
                customerProducer.sendMessage(orderEventDto);
                // updateKafkaTemplate.send(UPDATE_CUSTOMER_EVENT,updateCustomerEvent(event,"FAILED"));
            }
        }
        if (event.getStatus().equalsIgnoreCase(EventStatus.DELETING.name())) {
            OrderEventDto orderEventDto = new OrderEventDto();
            boolean customerExist = customerRepository.existsByCustomerIdEventAndStatus(event.getCustomer().getId(), EventStatus.CREATED.name());
            if (customerExist) {
                Customer customer= customerRepository.findCustomerByCustomerIdEvent(event.getCustomer().getId());
                customerRepository.deleteCustomer(customer.getCustomerIdEvent());
                //verifying if exists customer object
                boolean customerDeletedExist = customerRepository.existsByCustomerIdEventAndStatus(event.getCustomer().getId(), EventStatus.CREATED.name());
                if(!customerDeletedExist){
                    orderEventDto.setStatus(EventStatus.DELETED.name());
                    orderEventDto.setId(event.getCustomer().getId());
                    // event.setStatus(STATUS);
                    // event.setMessage("customer status is in created state");
                    LOGGER.info("Customer Update event with deleted status sent to Customer service => {}", orderEventDto);
                    customerProducer.sendMessage(orderEventDto);
                }

            }
        }
        if (event.getStatus().equalsIgnoreCase(EventStatus.UPDATING.name())) {
            OrderEventDto orderEventDto = new OrderEventDto();
            boolean customerExist = customerRepository.existsByCustomerIdEventAndStatus(event.getCustomer().getId(), EventStatus.CREATED.name());
            if (customerExist) {
                Customer customer = customerRepository.findCustomerByCustomerIdEvent(event.getCustomer().getId());
                customerRepository.updateCustomer(event.getCustomer().getId(),EventStatus.CREATED.name(),event.getCustomer().getName(),event.getCustomer().getPhone(),event.getCustomer().getEmail(),event.getCustomer().getAddress());
                orderEventDto.setStatus(EventStatus.UPDATED.name());
                orderEventDto.setId(event.getCustomer().getId());
                orderEventDto.setName(event.getCustomer().getName());

                CustomerEventDto customerEventDto =new CustomerEventDto();

                customerEventDto.setEmail(event.getCustomer().getEmail());
                customerEventDto.setPhone(event.getCustomer().getPhone());
                customerEventDto.setAddress(event.getCustomer().getAddress());
                orderEventDto.setCustomerEventDto(customerEventDto);
                event.setMessage("customer status is in updated state");
                LOGGER.info(String.format("Customer Update event with updated status sent to Customer service => %s", orderEventDto));
                customerProducer.sendMessage(orderEventDto);
            }
        }

    }
   /* private void updateCustomerEvent(CustomerEvent customerEvent,String status){
        UpdateCustomerEvent updateCustomerEvent=new UpdateCustomerEvent();
        updateCustomerEvent.setId(customerEvent.getCustomer().getId());
        updateCustomerEvent.setStatus(status);

        updateKafkaTemplate.send(UPDATE_CUSTOMER_EVENT,updateCustomerEvent);
        LOGGER.info(String.format("Customer Update event sent to Customer service => %s", updateCustomerEvent.toString()));

    }*/
}
