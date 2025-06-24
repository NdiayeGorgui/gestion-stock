package com.gogo.order_service.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "productitems")
public class ProductItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  Long id;
    private String productIdEvent;
    @Transient
    private Product product;
    private int quantity;
    private  double price;
    private double discount;
    private String orderIdEvent;
    //private String orderItemId;
    @ManyToOne
    private Order order;
    @CreationTimestamp
    private LocalDateTime createdDate;


    public double getAmount(){
        return ((price*quantity)-discount);
    }
}
