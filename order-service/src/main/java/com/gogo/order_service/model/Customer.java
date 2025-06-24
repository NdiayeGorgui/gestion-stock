package com.gogo.order_service.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "customers")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String customerIdEvent;
    private String name;
    private String address;
    private String city;
    private String postalCode;
    private String phone;
    private String email;
    private String status;
    @CreationTimestamp
    private LocalDateTime createdDate;

}
