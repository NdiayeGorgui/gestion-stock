package com.gogo.order_service.dto;

import java.util.UUID;

public class OrderNumberGenerator {
    public static String generateOrderNumber() {
        return "CMD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}

