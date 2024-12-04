package com.gogo.base_domaine_service.event;

import com.gogo.base_domaine_service.dto.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductEvent {
  //  private String eventType;
    private String message;
    private String status;
    private Product product;
}
