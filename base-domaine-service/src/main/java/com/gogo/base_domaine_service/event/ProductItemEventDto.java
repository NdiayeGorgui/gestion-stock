package com.gogo.base_domaine_service.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductItemEventDto {
    private String productIdEvent;
    private String productName;
    private String productItemStatus;
    private int qty;
    private double price;
    private double discount;

}
