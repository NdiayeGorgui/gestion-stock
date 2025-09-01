package com.gogo.mcp_java_server.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Data
@Table(name = "delivers")
public class Delivered {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String orderId;
   // private String paymentId;
    private String customerId;
    private String customerName;
    private String customerMail;
    private String status;
    private String details;
    @CreationTimestamp
    private LocalDateTime eventTimeStamp;
}
