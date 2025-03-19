package com.gogo.base_domaine_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Payment {
    private String paymentIdEvent;
    private String orderIdEvent;
    private String orderId;
    @NotBlank
    private String customerIdEvent;
    private String customerName;
    private String customerMail;
    @NotBlank
    private String paymentMode;
    private double amount;
    private String paymentStatus;
}
