package com.gogo.order_service.mapper;

import com.gogo.base_domaine_service.event.*;
import com.gogo.order_service.dto.ProductItemRequest;
import com.gogo.order_service.model.Customer;
import com.gogo.order_service.model.Product;
import com.gogo.order_service.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

public class OrderMapper {
    @Autowired
    private OrderService orderService;

    public static Customer mapToCustomerModel(CustomerEvent customerEvent){
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

    public static Product mapToProductModel(ProductEvent productEvent){
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

    public static CustomerEventDto mapToCustomerEventDto(CustomerEvent customerEvent){

        return new CustomerEventDto(
                customerEvent.getCustomer().getCustomerIdEvent(),
                null,
                customerEvent.getCustomer().getName(),
                customerEvent.getCustomer().getPhone(),
                customerEvent.getCustomer().getEmail(),
                customerEvent.getCustomer().getAddress(),
                customerEvent.getCustomer().getCity(),
                customerEvent.getCustomer().getPostalCode()
        );
    }

    public static ProductEventDto mapToProductEventDto(ProductEvent productEvent){
        return new ProductEventDto(
                productEvent.getProduct().getProductIdEvent(),
                null,
                productEvent.getProduct().getName(),
                productEvent.getProduct().getCategory(),
                productEvent.getProduct().getDescription(),
                productEvent.getProduct().getLocation(),
                productEvent.getProduct().getQty(),
                productEvent.getProduct().getPrice(),
                productEvent.getProduct().getQtyStatus()
        );
    }

    public static CustomerEventDto mapToCustomerEventDto(OrderEvent orderEvent){
        return new CustomerEventDto(
                orderEvent.getCustomer().getCustomerIdEvent(),
                null,
                orderEvent.getCustomer().getName(),
                orderEvent.getCustomer().getPhone(),
                orderEvent.getCustomer().getEmail(),
                orderEvent.getCustomer().getAddress(),
                orderEvent.getCustomer().getCity(),
                orderEvent.getCustomer().getPostalCode()
        );
    }

    public static ProductEventDto mapToProductEventDto(OrderEvent orderEvent){
        return new ProductEventDto(
                orderEvent.getProduct().getProductIdEvent(),
                null,
                orderEvent.getProduct().getName(),
                orderEvent.getProduct().getCategory(),
                orderEvent.getProduct().getDescription(),
                orderEvent.getProduct().getLocation(),
                orderEvent.getProduct().getQty(),
                orderEvent.getProduct().getPrice(),
                orderEvent.getProduct().getQtyStatus()
        );
    }

    public static ProductItemEventDto mapToProductItemEventDto(OrderEvent orderEvent){
        return new ProductItemEventDto(
                orderEvent.getProductItem().getProductId(),
                orderEvent.getProduct().getName(),
                null,
                orderEvent.getProductItem().getProductQty(),
                orderEvent.getProductItem().getProductPrice(),
                orderEvent.getProductItem().getDiscount()
        );
    }

    public ProductItemEventDto convertToProductItemEventDto(ProductItemRequest request, Product product) {
        ProductItemEventDto dto = new ProductItemEventDto();
        dto.setProductIdEvent(request.getProductIdEvent());
        dto.setQty(request.getProductQty());
        dto.setPrice(product.getPrice());
        dto.setDiscount(orderService.getAmount(request.getProductQty(), product.getPrice()));
        dto.setProductItemStatus("PENDING"); // ou une autre logique métier
        return dto;
    }

}

