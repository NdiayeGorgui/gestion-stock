package com.gogo.mcp_java_server.model;

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
    private String customerId;
    private String customerName;
    private String customerPhone;
    private String customerMail;
    private String orderRef;
    private String productId;
    private String productName;
    private int quantity;
    private double price;
    private double discount;
    private String status;
}
