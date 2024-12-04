package com.gogo.base_domaine_service.dto;

import com.gogo.base_domaine_service.event.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDto {
    private String orderEventId;
    private OrderStatus status;
}
