package com.gogo.order_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AmountDto {
    double amount;
    double totalAmount;
    double tax;
    double discount;
}
