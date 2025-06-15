package com.gogo.notification_service.service;

import com.gogo.notification_service.dto.NotificationDto;
import com.gogo.notification_service.mapper.NotificationMapper;
import com.gogo.notification_service.model.Notification;
import com.gogo.notification_service.repository.NotificationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;


    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }


    public void markAsRead(Long id, String username) {
        Notification notif = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found with id : " + id));

        if (!notif.getUsername().equals(username) && !notif.getUsername().equals("ALL_USERS")) {
            throw new RuntimeException("User not authorized to modify this notification");
        }

        notif.setReadValue(true);
        notificationRepository.save(notif);
    }


    public void archiveNotification(Long id, String username) {
        Notification notif = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notif.getUsername().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot archive this notification");
        }

        notif.setArchived(true);
        notificationRepository.save(notif);
    }


    public List<NotificationDto> getUserAndGlobalNotifications(String username) {
        List<Notification> userNotifs = notificationRepository.findByUsernameOrderByIdDesc(username);
        List<Notification> globalNotifs = notificationRepository.findByUsernameOrderByIdDesc("allusers");

        List<Notification> all = new ArrayList<>();
        all.addAll(userNotifs);
        all.addAll(globalNotifs);

        return all.stream()
                .map(NotificationMapper::fromEntity)
                .collect(Collectors.toList());
    }


}

