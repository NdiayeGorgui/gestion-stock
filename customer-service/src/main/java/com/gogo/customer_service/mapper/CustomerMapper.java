package com.gogo.customer_service.mapper;

import com.gogo.base_domaine_service.dto.Customer;
import com.gogo.base_domaine_service.event.EventStatus;
import com.gogo.customer_service.model.CustomerModel;

import java.time.LocalDateTime;
import java.util.UUID;


public class CustomerMapper {

    public static CustomerModel mapToCustomerModel(Customer customer){

        return new CustomerModel(
                null,
                UUID.randomUUID().toString(),
                customer.getName(),
                customer.getAddress(),
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
                customerModel.getPhone(),
                customerModel.getEmail()
        );
    }
}
