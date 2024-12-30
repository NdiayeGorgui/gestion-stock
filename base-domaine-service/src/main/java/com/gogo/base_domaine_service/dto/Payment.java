package com.gogo.base_domaine_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Payment {
    private String paymentIdEvent;
    private String orderIdEvent;
    private String customerIdEvent;
    private String paymentMode;
    private double amount;
    private String paymentStatus;
}
