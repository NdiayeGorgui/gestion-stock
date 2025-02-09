package com.gogo.order_service.model;

import jakarta.persistence.*;
import lombok.*;

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
    @ManyToOne
    private Order order;

    public double getAmount(){
        return ((price*quantity)-discount);
    }
}
