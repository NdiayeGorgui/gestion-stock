package com.gogo.base_domaine_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Product {

    private  String productIdEvent;
    @NotBlank
    private String name;
    private String description;
    private String location;
    private String category;
    @Min(1)
    private int qty;
    @Positive
    private  double price;
    private String qtyStatus;
}
