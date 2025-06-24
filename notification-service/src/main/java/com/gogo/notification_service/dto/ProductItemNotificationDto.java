package com.gogo.notification_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductItemNotificationDto {
    private String productIdEvent;
    private String productName;
    private int qty;          // quantité commandée
    private int initialQty;   // quantité en stock au moment de la commande
}

