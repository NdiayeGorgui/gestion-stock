package com.gogo.payment_service.dto;

import java.util.UUID;

public class PaymentNumberGenerator {
    public static String generatePaymentNumber() {
        return "PMT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}

