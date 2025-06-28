package com.gogo.order_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class OrderResponseDto {
    private String orderId;
    private String customerName;
    private String customerEmail;
    private double amount;
    private double totalTax;
    private double totalDiscount;
    private LocalDateTime createdDate;
    private List<ProductItemResponseDto> items;
}

