package com.gogo.customer_service.mapper;

import com.gogo.base_domaine_service.dto.Customer;
import com.gogo.base_domaine_service.event.EventStatus;
import com.gogo.customer_service.dto.CustomerNumberGenerator;
import com.gogo.customer_service.model.CustomerModel;

import java.time.LocalDateTime;
import java.util.UUID;


public class CustomerMapper {

    public static CustomerModel mapToCustomerModel(Customer customer){
        String customerId = CustomerNumberGenerator.generateCustomerNumber();

        return new CustomerModel(
                null,
                customerId,
                customer.getName(),
                customer.getAddress(),
                customer.getCity(),
                customer.getPostalCode(),
                customer.getPhone(),
                customer.getEmail(),
                EventStatus.PENDING.name(),
                LocalDateTime.now()
        );
    }

    public static Customer mapToCustomer(CustomerModel customerModel){

        return new Customer(
                customerModel.getCustomerIdEvent(),
                customerModel.getName(),
                customerModel.getAddress(),
                customerModel.getCity(),
                customerModel.getPostalCode(),
                customerModel.getPhone(),
                customerModel.getEmail()
        );
    }
}
