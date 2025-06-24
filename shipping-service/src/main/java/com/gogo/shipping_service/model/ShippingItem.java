package com.gogo.shipping_service.model;/*
package com.gogo.shipping_service.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class ShippingItem {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ship_id")
    private Ship ship;

    private String productIdEvent;
    private String productName;
    private int quantity;
    private double price;
    @Column(nullable = false, columnDefinition = "double default 0.0")
    private double discount;
}

*/
