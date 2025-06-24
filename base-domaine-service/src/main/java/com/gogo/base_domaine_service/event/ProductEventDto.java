package com.gogo.base_domaine_service.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductEventDto {
    private String productIdEvent;
    private String productStatus;
    private String name;
    private String category;
    private String description;
    private String location;
    private int qty;
    private double price;
    private String qtyStatus;
}
