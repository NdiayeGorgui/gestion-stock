package com.gogo.base_domaine_service.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderEventDto {
    private String id;
    private String status;
    private String paymentId;
    private CustomerEventDto customerEventDto;
    private ProductEventDto productEventDto;
    private ProductItemEventDto productItemEventDto;
}