package com.gogo.billing_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductItemDto {
    private String productId;
    private String productName;
    private int quantity;
    private double price;
    private double discount;
    private double tax;
}
