package com.gogo.inventory_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "products")
public class ProductModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  Long id;
    private String productIdEvent;
    private String name;
    private String category;
    private String description;
    private String location;
    private int qty;
    private  double price;
    private String status;
    private String qtyStatus;
    @CreationTimestamp
    private LocalDateTime createdDate;

}
