package com.gogo.notification_service.repository;

import com.gogo.notification_service.model.Notification;
import com.gogo.notification_service.model.UserNotificationRead;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserNotificationReadRepository extends JpaRepository<UserNotificationRead, Long> {
    boolean existsByNotificationAndUsername(Notification notification, String username);
}

