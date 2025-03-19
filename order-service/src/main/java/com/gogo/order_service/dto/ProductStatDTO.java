package com.gogo.order_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProductStatDTO {
	
	private String productIdEvent;
    private String name;
    private Long totalQuantite;

}
