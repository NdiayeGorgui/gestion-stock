package com.gogo.order_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AmountDto {
    @Schema(description = "Amount")
    double amount;
    @Schema(description = "Total amount")
    double totalAmount;
    @Schema(description = "Tax")
    double tax;
    @Schema(description = "Discount")
    double discount;
}
