package com.gogo.mcp_java_server.dto;

import java.time.LocalDateTime;

public record ShipDto(String orderId,
                     // String paymentId,
                      String customerId,
                      String customerName,
                      String customerMail,
                      String status,
                      String details,
                      LocalDateTime eventTimeStamp) { }
