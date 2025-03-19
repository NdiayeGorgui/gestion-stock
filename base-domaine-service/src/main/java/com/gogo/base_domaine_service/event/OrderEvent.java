package com.gogo.base_domaine_service.event;

import com.gogo.base_domaine_service.dto.Customer;
import com.gogo.base_domaine_service.dto.Product;
import com.gogo.base_domaine_service.dto.ProductItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderEvent {
    private String orderIdEvent;
    private String message;
    private String status;
    private Customer customer;
    private Product product;
    private ProductItem productItem;
}
