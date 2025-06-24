package com.gogo.order_service.dto;

import com.gogo.order_service.model.Customer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommandEvent {
    private Customer customer;
    private List<ProductItemRequest> productItems;
}
