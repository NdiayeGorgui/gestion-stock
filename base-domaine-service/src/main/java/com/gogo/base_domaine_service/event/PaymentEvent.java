package com.gogo.base_domaine_service.event;

import com.gogo.base_domaine_service.dto.Payment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentEvent {
    private String message;
    private String status;
    private Payment payment;
}
