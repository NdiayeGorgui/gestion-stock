package com.gogo.order_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CustomerDto {
	private String cutomerIdEvent;
    private String name;
    private Long totalOrder;

}
