package com.gogo.customer_service.service;

import com.gogo.base_domaine_service.dto.Customer;
import com.gogo.base_domaine_service.event.CustomerEvent;
import com.gogo.base_domaine_service.event.EventStatus;
import com.gogo.customer_service.kafka.CustomerProducer;
import com.gogo.customer_service.mapper.CustomerMapper;
import com.gogo.customer_service.model.CustomerModel;
import com.gogo.customer_service.repository.CustomerRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class CustomerService {
    @Autowired
    private CustomerRepository customerRepository;

    private final CustomerProducer customerProducer;

    public CustomerService(CustomerProducer customerProducer) {
        this.customerProducer = customerProducer;
    }

    public void saveCustomer(CustomerModel customer){
        customerRepository.save(customer);
    }

    public void saveAndSendCustomer(Customer customer){
        CustomerModel savedCustomer= CustomerMapper.mapToCustomerModel(customer);
        this.saveCustomer(savedCustomer);

        customer.setCustomerIdEvent(savedCustomer.getCustomerIdEvent());
        CustomerEvent customerEvent = new CustomerEvent();
        customerEvent.setStatus(EventStatus.PENDING.name());
        customerEvent.setMessage("customer status is in pending state");
        customerEvent.setCustomer(customer);

        customerProducer.sendMessage(customerEvent);
    }

    public void sendCustomerToDelete(String customerIdEvent){
        CustomerModel customerModel=customerRepository.findCustomerByCustomerIdEvent(customerIdEvent);

        Customer customer=CustomerMapper.mapToCustomer(customerModel);

        CustomerEvent customerEvent=new CustomerEvent();

        customerEvent.setStatus(EventStatus.DELETING.name());
        customerEvent.setMessage("customer status is in deleting state");
        customerEvent.setCustomer(customer);

        customerProducer.sendMessage(customerEvent);

    }

    public void sendCustomerToUpdate(String customerIdEvent, Customer customer){
        CustomerModel customerModel=customerRepository.findCustomerByCustomerIdEvent(customerIdEvent);

        customer.setCustomerIdEvent(customerModel.getCustomerIdEvent());

        CustomerEvent customerEvent=new CustomerEvent();

        customerEvent.setStatus(EventStatus.UPDATING.name());
        customerEvent.setMessage("customer status is in updating state");
        customerEvent.setCustomer(customer);

        customerProducer.sendMessage(customerEvent);
    }

    @Transactional
   public void updateCustomerStatus(String customerIdEvent, String status ){
        customerRepository.updateCustomerStatus(customerIdEvent, status);

    }
    public void updateCustomer(String customerIdEvent, String status , String name, String phone, String email, String address, String city, String postalCode){
        customerRepository.updateCustomer(customerIdEvent, status, name, phone, email, address,city,postalCode);

    }
    public void deleteCustomer(String customerIdEvent,String status ){
         customerRepository.deleteCustomer(customerIdEvent,status);

    }

    public CustomerModel findCustomerById(String customerIdEvent){
        return customerRepository.findCustomerByCustomerIdEvent(customerIdEvent);
    }

    public List<CustomerModel> getAllCustomers() {
        return customerRepository.findAll();
    }

    public boolean existsByEmail(String email) {
        return customerRepository.existsByEmail(email);
    }
}
