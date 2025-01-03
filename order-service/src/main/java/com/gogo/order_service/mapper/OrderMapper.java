package com.gogo.order_service.mapper;

import com.gogo.base_domaine_service.event.*;
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

    public static CustomerEventDto mapToCustomerEventDto(CustomerEvent customerEvent){

        return new CustomerEventDto(
                customerEvent.getCustomer().getId(),
                null,
                customerEvent.getCustomer().getName(),
                customerEvent.getCustomer().getPhone(),
                customerEvent.getCustomer().getEmail(),
                customerEvent.getCustomer().getAddress()
        );
    }

    public static ProductEventDto mapToProductEventDto(ProductEvent productEvent){
        return new ProductEventDto(
                productEvent.getProduct().getId(),
                null,
                productEvent.getProduct().getName(),
                productEvent.getProduct().getQty(),
                productEvent.getProduct().getPrice(),
                productEvent.getProduct().getQtyStatus()
        );
    }

    public static CustomerEventDto mapToCustomerEventDto(OrderEvent orderEvent){
        return new CustomerEventDto(
                orderEvent.getCustomer().getId(),
                null,
                orderEvent.getCustomer().getName(),
                orderEvent.getCustomer().getPhone(),
                orderEvent.getCustomer().getEmail(),
                orderEvent.getCustomer().getAddress()
        );
    }

    public static ProductEventDto mapToProductEventDto(OrderEvent orderEvent){
        return new ProductEventDto(
                orderEvent.getProduct().getId(),
                null,
                orderEvent.getProduct().getName(),
                orderEvent.getProduct().getQty(),
                orderEvent.getProduct().getPrice(),
                EventStatus.MODIFYING.name()
        );
    }

    public static ProductItemEventDto mapToProductItemEventDto(OrderEvent orderEvent){
        return new ProductItemEventDto(
                orderEvent.getProductItem().getProductId(),
                null,
                orderEvent.getProductItem().getProductQty(),
                orderEvent.getProductItem().getProductPrice(),
                orderEvent.getProductItem().getDiscount()
        );
    }
}

