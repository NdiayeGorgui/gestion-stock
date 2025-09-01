package com.gogo.mcp_java_server.dto;

import java.time.LocalDateTime;

public record BillDto(String productId,
                      String customerId,
                      String customerName,
                      String customerPhone,
                      String customerMail,
                      String orderRef,
                      int quantity,
                      double price,
                      double discount,
                      String status,
                      LocalDateTime createdDate) { }
