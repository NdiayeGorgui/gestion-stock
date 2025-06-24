package com.gogo.notification_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderEventForNotificationDto {
    private String id;
    private String status;
    private String userName;
    private String paymentId;
    private List<ProductEventNotificationDto> productEventNotificationDtos;
    private List<ProductItemNotificationDto> productItemEventDtos;
}

