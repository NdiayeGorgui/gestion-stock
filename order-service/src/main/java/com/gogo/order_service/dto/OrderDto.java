package com.gogo.order_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class OrderDto {
	@NotBlank
	private String customerIdEvent;
	@NotBlank
	private String productIdEvent;
	@NotNull
	private int productItemQty;
	
}
