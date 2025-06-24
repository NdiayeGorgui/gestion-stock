package com.gogo.customer_service.dto;

import java.util.UUID;

public class CustomerNumberGenerator {
    public static String generateCustomerNumber() {
        return "CUST-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}

