package com.gogo.delivered_query_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
    private String customerMail;
    private String orderRef;
    //private String orderId;
    private String productIdEvent;
    private String productName;
    private int quantity;
    private double price;
    private double discount;
    private String status;
}
