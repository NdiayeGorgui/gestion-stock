package com.gogo.order_service.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "products")
public class Product {
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
