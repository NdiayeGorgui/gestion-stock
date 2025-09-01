package com.gogo.mcp_java_server.dto;

import java.time.LocalDateTime;

public record OrderDto(String orderId,
                       LocalDateTime date,
                       String customerId,
                       String orderStatus) { }
