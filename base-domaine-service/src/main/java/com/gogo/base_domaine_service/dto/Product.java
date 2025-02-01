package com.gogo.base_domaine_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Product {

    private  String productIdEvent;
    private String name;
    private int qty;
    private  double price;
    private String qtyStatus;
}
