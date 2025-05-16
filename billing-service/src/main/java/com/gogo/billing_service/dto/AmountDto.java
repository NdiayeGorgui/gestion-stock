package com.gogo.billing_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AmountDto {
    @Schema(description = "Amount")
    private double amount;
    @Schema(description = "Total amount")
    private double totalAmount;
    @Schema(description = "Tax")
    private double tax;
    @Schema(description = "Discount")
    private double discount;
}
