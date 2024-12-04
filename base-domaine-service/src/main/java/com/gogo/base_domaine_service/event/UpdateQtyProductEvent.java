package com.gogo.base_domaine_service.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateQtyProductEvent {
    private String id;
    private String message;
    private String status;
    private String qtyStatus;
    private int qty;
}
