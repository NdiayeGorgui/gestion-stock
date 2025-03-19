package com.gogo.shipping_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "ships")
public class Ship {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String orderId;
    private String paymentId;
    private String customerId;
    private String customerName;
    private String customerMail;
    private String status;
    private String details;
    private LocalDateTime eventTimeStamp;
}
