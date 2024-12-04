
package com.gogo.billing_service.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gogo.base_domaine_service.dto.Customer;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Collection;
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name="bills")
public class Bill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime billingDate;
  //  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) //car id de customer apparait deja dans la facture(au niveau de customer)
    private String customerIdEvent;
    private String customerName;
    private String customerPhone;
    private String orderRef;
    private String productIdEvent;
    private String productName;
    private int quantity;
    private double price;
    private double discount;
    private String status;
}

