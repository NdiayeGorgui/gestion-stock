package com.gogo.payment_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "payments")
public class PaymentModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String paymentIdEvent;
    private String customerIdEvent;
    private String orderId;
    private String customerName;
    private String customerMail;
    private String paymentMode;
    private double amount;
    private LocalDateTime timeStamp;
    private String paymentStatus;

}
