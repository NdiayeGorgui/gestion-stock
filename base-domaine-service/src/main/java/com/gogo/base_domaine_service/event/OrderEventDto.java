package com.gogo.base_domaine_service.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderEventDto {
    private String id;
    private String status;
    private String userName;
    private String paymentId;
    private CustomerEventDto customerEventDto;
    private ProductEventDto productEventDto;
    //private ProductItemEventDto productItemEventDto;
    // ✅ Liste d'articles, chaque article contient un produit
    private List<ProductItemEventDto> productItemEventDtos;
    private List<ProductEventDto> ProductEventDtos;
}