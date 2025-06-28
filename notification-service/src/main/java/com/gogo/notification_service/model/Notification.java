package com.gogo.notification_service.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "notifications")

public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username; // le destinataire
    private String message;
    private boolean readValue;
    private String type; // e.g., "global" ou "user"
    @Column(nullable = false)
    private boolean archived = false;
    @CreationTimestamp
    private LocalDateTime createdDate;
    private String productKey;

}


