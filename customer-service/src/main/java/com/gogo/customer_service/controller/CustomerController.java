package com.gogo.customer_service.controller;

import com.gogo.base_domaine_service.dto.Customer;

import com.gogo.customer_service.dto.CustomerExistsResponse;
import com.gogo.customer_service.exception.CustomerNotFoundException;
import com.gogo.customer_service.kafka.CustomerProducer;
import com.gogo.customer_service.model.CustomerModel;
import com.gogo.customer_service.repository.CustomerRepository;
import com.gogo.customer_service.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

//@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:4300"})
@RestController
@RequestMapping("/api/v1")
public class CustomerController {
    @Autowired
    private CustomerService customerService;

    private final CustomerProducer customerProducer;

    public CustomerController(CustomerProducer customerProducer) {
        this.customerProducer = customerProducer;
    }

    @Operation(
            summary = "save and send Customer REST API",
            description = "save and send Customer REST API to customer object")
    @ApiResponse(responseCode = "200",
            description = "Http status 200 ")

    @PostMapping("/customers")
    public ResponseEntity<Map<String, String>> saveAndSendCustomer(@RequestBody @Valid Customer customer) {
        customerService.saveAndSendCustomer(customer);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Customer sent successfully");
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "get Customers REST API",
            description = "get Customers REST API from CustomerModel object")
    @ApiResponse(responseCode = "200",
            description = "Http status 200 ")

    @GetMapping("/customers")
    public List<CustomerModel> getCustomers() {
        return customerService.getAllCustomers();

    }

    @Operation(
            summary = "update Customer REST API",
            description = "update and send Customer by customerIdEvent REST API from CustomerModel object")
    @ApiResponse(responseCode = "200",
            description = "Http status 200 ")

    @PutMapping("/customers/{customerIdEvent}")
    public ResponseEntity<Map<String, String>> updateAndSendCustomer(@RequestBody Customer customer, @PathVariable ("customerIdEvent") String customerIdEvent) throws CustomerNotFoundException {
        CustomerModel customerModel = customerService.findCustomerById(customerIdEvent);

        if (customerModel != null) {
            customerService.sendCustomerToUpdate(customerIdEvent, customer);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Customer sent successfully");
            return ResponseEntity.ok(response);
        }
        throw new CustomerNotFoundException("Customer not available with id: " + customerIdEvent);

    }

    @Operation(
            summary = "delete Customer REST API",
            description = "delete and send Customer by customerIdEvent REST API from CustomerModel object")
    @ApiResponse(responseCode = "200",
            description = "Http status 200 ")

    @DeleteMapping("/customers/{customerIdEvent}")
    public ResponseEntity<Map<String, String>> sendCustomer(@PathVariable ("customerIdEvent") String customerIdEvent) throws CustomerNotFoundException {
        CustomerModel customerModel = customerService.findCustomerById(customerIdEvent);
        if (customerModel != null) {
            customerService.sendCustomerToDelete(customerIdEvent);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Customer sent successfully");
            return ResponseEntity.ok(response);
        }
        throw new CustomerNotFoundException("Customer not available with id: " + customerIdEvent);
    }

    @Operation(
            summary = "get Customer REST API",
            description = "get Customer by customerIdEvent REST API from CustomerModel object")
    @ApiResponse(responseCode = "200",
            description = "Http status 200 ")

    @GetMapping("/customers/{customerIdEvent}")
    public CustomerModel getCustomer( @PathVariable ("customerIdEvent") String customerIdEvent){
        return customerService.findCustomerById(customerIdEvent);
    }

    @GetMapping("/customers/exists-by-email/{email}")
    public ResponseEntity<CustomerExistsResponse> checkIfCustomerExistsByEmail(@PathVariable ("email") String email) {
        boolean exists = customerService.existsByEmail(email);
        String message = exists ? "The customer with this email already exists." : "Email is available.";

        CustomerExistsResponse response = new CustomerExistsResponse(exists, message);
        return ResponseEntity.ok(response);
    }
}
