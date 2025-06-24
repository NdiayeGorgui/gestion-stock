package com.gogo.order_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProductItemResponseDto {
    private String productId;
    private String productName;
    private int quantity;
    private double price;
    private double discount;
    private double tax;
}
