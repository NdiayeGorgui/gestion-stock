package com.gogo.base_domaine_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductItem {
    private  String productId;
    private  int productQty;
    private double productPrice;
    private double discount;
}
