package com.gogo.billing_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BillResponseDto {
    private String orderId;
    private String customerName;
    private String customerPhone;
    private String customerMail;
    private double amount;
    private double totalTax;
    private double totalDiscount;
    private String billStatus;
    private LocalDateTime billingDate;

    private List<ProductItemDto> products;
}

