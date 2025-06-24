package com.gogo.payment_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResponseDto {
    private String paymentIdEvent;
    private String orderId;
    private String customerName;
    private String customerMail;
    private String paymentMode;
    private double amount;
    private double totalTax;
    private double totalDiscount;
    private String paymentStatus;
    private LocalDateTime timeStamp;

    private List<ProductItemDto> products;
}

