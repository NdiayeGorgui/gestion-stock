package com.gogo.order_service.mapper;

import com.gogo.base_domaine_service.event.CustomerEvent;
import com.gogo.base_domaine_service.event.EventStatus;
import com.gogo.base_domaine_service.event.ProductEvent;
import com.gogo.order_service.model.Product;

public class OrderMapper {

    public static com.gogo.order_service.model.Customer mapToCustomerModel(CustomerEvent customerEvent){
        return new com.gogo.order_service.model.Customer(
                null,
                customerEvent.getCustomer().getId(),
                customerEvent.getCustomer().getName(),
                customerEvent.getCustomer().getAddress(),
                customerEvent.getCustomer().getPhone(),
                customerEvent.getCustomer().getEmail(),
                EventStatus.CREATED.name()
        );
    }

    public static com.gogo.order_service.model.Product mapToProductModel(ProductEvent productEvent){
        return new Product(
                null,
                productEvent.getProduct().getId(),
                productEvent.getProduct().getName(),
                productEvent.getProduct().getQty(),
                productEvent.getProduct().getPrice(),
                EventStatus.CREATED.name(),
                EventStatus.AVAILABLE.name()
        );
    }
}
