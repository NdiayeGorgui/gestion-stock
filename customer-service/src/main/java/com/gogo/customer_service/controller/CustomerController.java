package com.gogo.customer_service.controller;

import com.gogo.base_domaine_service.dto.Customer;
import com.gogo.base_domaine_service.event.CustomerEvent;
import com.gogo.customer_service.kafka.CustomerProducer;
import com.gogo.customer_service.model.CustomerModel;
import com.gogo.customer_service.repository.CustomerRepository;
import com.gogo.customer_service.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class CustomerController {
    @Autowired
    private CustomerService customerService;
    @Autowired
    private CustomerRepository customerRepository;

    private final CustomerProducer customerProducer;

    public CustomerController(CustomerProducer customerProducer) {
        this.customerProducer = customerProducer;
    }

    @PostMapping("/customers")
    public String saveAndSendCustomer(@RequestBody Customer customer){
      customerService.saveAndSendCustomer(customer);

        return "Customer sent successfully ...";
    }

    @PutMapping("/customers/{customerIdEvent}")
    public String updateAndSendCustomer(@RequestBody Customer customer,@PathVariable String customerIdEvent){
        customerService.sendCustomerToUpdate(customerIdEvent,customer);

        return "Customer sent successfully ...";
    }

    @DeleteMapping("/customers/{customerIdEvent}")
    public String sendCustomer(@PathVariable String customerIdEvent){

       customerService.sendCustomerToDelete(customerIdEvent);
        return "Customer sent successfully ...";
    }
}
