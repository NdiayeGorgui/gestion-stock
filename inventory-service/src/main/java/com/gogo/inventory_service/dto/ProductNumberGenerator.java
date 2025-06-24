package com.gogo.inventory_service.dto;

import java.util.UUID;

public class ProductNumberGenerator {
    public static String generateProductNumber() {
        return "PROD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}

