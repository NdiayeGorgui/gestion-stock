package com.gogo.delivered_command_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeliveredResponseDto {
    private String paymentId;
    private String orderId;
    private String customerName;
    private String customerMail;
    private double amount;
    private double totalTax;
    private double totalDiscount;
    private String deliveryStatus;
    private LocalDateTime timeStamp;

    private List<ProductItemDto> products;


}
