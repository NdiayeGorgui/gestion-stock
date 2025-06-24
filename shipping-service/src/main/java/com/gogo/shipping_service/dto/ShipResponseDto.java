package com.gogo.shipping_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShipResponseDto {
    private String orderId;
    private String paymentId;
    private String customerName;
    private String customerMail;
    private double amount;
    private double totalTax;
    private double totalDiscount;
    private String shippingStatus;
    private LocalDateTime eventTimeStamp;

    private List<ProductItemDto> products;

}