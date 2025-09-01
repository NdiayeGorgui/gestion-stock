package com.gogo.mcp_java_server.dto;

import java.time.LocalDateTime;

public record CustomerDto(String customerId,
                          String name,
                          String address,
                          String city,
                          String postalCode,
                          String phone,
                          String email,
                          String status,
                          LocalDateTime createdDate) { }
