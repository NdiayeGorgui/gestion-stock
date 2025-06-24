package com.gogo.notification_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductEventNotificationDto {
    private String productIdEvent;
    private int qty; // Stock initial
}

