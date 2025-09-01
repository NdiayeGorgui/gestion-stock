package com.gogo.mcp_java_server.mapper;


import com.gogo.base_domaine_service.event.*;
import com.gogo.mcp_java_server.model.Bill;
import com.gogo.mcp_java_server.model.Customer;
import com.gogo.mcp_java_server.model.Product;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public class MpcMapper {
    public static List<Bill> mapToBills(OrderEventDto event) {
        if (event.getProductItemEventDtos() == null || event.getCustomerEventDto() == null) {
            throw new IllegalArgumentException("ProductItemEventDtos or CustomerEventDto is null");
        }

        List<Bill> bills = new ArrayList<>();

        for (ProductItemEventDto item : event.getProductItemEventDtos()) {
            Bill bill = new Bill(
                    null,
                    LocalDateTime.now(),
                    event.getCustomerEventDto().getCustomerIdEvent(),
                    event.getCustomerEventDto().getName(),
                    event.getCustomerEventDto().getPhone(),
                    event.getCustomerEventDto().getEmail(),
                    event.getId(),
                   // event.getPaymentId(),
                    item.getProductIdEvent(),
                    item.getProductName(),
                    item.getQty(),
                    item.getPrice(),
                    item.getDiscount(),
                    event.getStatus()
            );
            bills.add(bill);
        }

        return bills;
    }

    public static Customer mapToCustomerEvent(CustomerEvent customerEvent) {
        return new Customer(
                null,
                customerEvent.getCustomer().getCustomerIdEvent(),
                customerEvent.getCustomer().getName(),
                customerEvent.getCustomer().getAddress(),
                customerEvent.getCustomer().getCity(),
                customerEvent.getCustomer().getPostalCode(),
                customerEvent.getCustomer().getPhone(),
                customerEvent.getCustomer().getEmail(),
                EventStatus.CREATED.name(),
                LocalDateTime.now()
        );
    }

    public static Product mapToProductEvent(ProductEvent productEvent) {
        return new Product(
                null,
                productEvent.getProduct().getProductIdEvent(),
                productEvent.getProduct().getName(),
                productEvent.getProduct().getCategory(),
                productEvent.getProduct().getDescription(),
                productEvent.getProduct().getLocation(),
                productEvent.getProduct().getQty(),
                productEvent.getProduct().getPrice(),
                EventStatus.CREATED.name(),
                productEvent.getProduct().getQtyStatus(),
                LocalDateTime.now()
        );
    }

}


