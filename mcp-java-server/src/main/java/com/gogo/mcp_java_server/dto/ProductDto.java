package com.gogo.mcp_java_server.dto;

import java.time.LocalDateTime;

public record ProductDto(String productId,
                         String name,
                         String category,
                         String description,
                         String location,
                         int qty,
                         double price,
                         String status,
                         String qtyStatus,
                         LocalDateTime createdDate) { }
