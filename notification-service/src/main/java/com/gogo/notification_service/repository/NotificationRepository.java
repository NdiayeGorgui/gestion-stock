package com.gogo.notification_service.repository;

import com.gogo.notification_service.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Récupérer les notifications par utilisateur
    List<Notification> findByUsernameOrderByIdDesc(String username);
}

