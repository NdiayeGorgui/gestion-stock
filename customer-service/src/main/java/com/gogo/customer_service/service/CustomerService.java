package com.gogo.customer_service.service;

import com.gogo.base_domaine_service.dto.Customer;
import com.gogo.base_domaine_service.event.CustomerEvent;
import com.gogo.base_domaine_service.event.OrderStatus;
import com.gogo.customer_service.kafka.CustomerProducer;
import com.gogo.customer_service.model.CustomerModel;
import com.gogo.customer_service.repository.CustomerRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

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
        CustomerModel savedCustomer=new CustomerModel();
        savedCustomer.setCustomerIdEvent(UUID.randomUUID().toString());
        savedCustomer.setName(customer.getName());
        savedCustomer.setAddress(customer.getAddress());
        savedCustomer.setEmail(customer.getEmail());
        savedCustomer.setStatus("PENDING");
        savedCustomer.setPhone(customer.getPhone());
        this.saveCustomer(savedCustomer);

        customer.setId(savedCustomer.getCustomerIdEvent());
        CustomerEvent customerEvent = new CustomerEvent();
        customerEvent.setStatus("PENDING");
        customerEvent.setMessage("customer status is in pending state");
        customerEvent.setCustomer(customer);

        customerProducer.sendMessage(customerEvent);
    }

    public void sendCustomerToDelete(String customerIdEvent){
        CustomerModel customerModel=customerRepository.findCustomerByCustomerIdEvent(customerIdEvent);
        Customer customer=new Customer();
        customer.setId(customerModel.getCustomerIdEvent());
        customer.setName(customerModel.getName());
        customer.setPhone(customerModel.getPhone());
        customer.setEmail(customerModel.getEmail());
        customer.setAddress(customerModel.getAddress());

        CustomerEvent customerEvent=new CustomerEvent();

        customerEvent.setStatus("DELETING");
        customerEvent.setMessage("customer status is in deleting state");
        customerEvent.setCustomer(customer);

        customerProducer.sendMessage(customerEvent);

    }

    public void sendCustomerToUpdate(String customerIdEvent, Customer customer){
        CustomerModel customerModel=customerRepository.findCustomerByCustomerIdEvent(customerIdEvent);

        customer.setId(customerModel.getCustomerIdEvent());
      //  customer.setName(customerModel.getName());
       // customer.setPhone(customerModel.getPhone());
      //  customer.setEmail(customerModel.getEmail());
       // customer.setAddress(customerModel.getAddress());

        CustomerEvent customerEvent=new CustomerEvent();

        customerEvent.setStatus("UPDATING");
        customerEvent.setMessage("customer status is in updating state");
        customerEvent.setCustomer(customer);

        customerProducer.sendMessage(customerEvent);

    }

    @Transactional
   public int updateCustomerStatus(String customerIdEvent,String status ){
        return customerRepository.updateCustomerStatus(customerIdEvent,status);

   }
    public int updateCustomer(String customerIdEvent,String status ,String name,String phone,String email,String address){
        return customerRepository.updateCustomer(customerIdEvent,status,name,phone,email,address);

    }
    public void deleteCustomer(String customerIdEvent,String status ){
         customerRepository.deleteCustomer(customerIdEvent,status);

    }


}
