package com.gogo.notification_service.service;

import com.gogo.notification_service.dto.NotificationDto;
import com.gogo.notification_service.mapper.NotificationMapper;
import com.gogo.notification_service.model.Notification;
import com.gogo.notification_service.model.UserNotificationRead;
import com.gogo.notification_service.repository.NotificationRepository;
import com.gogo.notification_service.repository.UserNotificationReadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    @Autowired
    private UserNotificationReadRepository userNotificationReadRepository;


    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }


    public void markAsRead(Long id, String username) {
        Notification notif = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found with id : " + id));

        if (notif.getUsername().equals("allusers")) {
            // Notification globale : marquer comme lue pour CET utilisateur
            boolean alreadyMarked = userNotificationReadRepository.existsByNotificationAndUsername(notif, username);
            if (!alreadyMarked) {
                UserNotificationRead readEntry = new UserNotificationRead();
                readEntry.setNotification(notif);
                readEntry.setUsername(username);
                userNotificationReadRepository.save(readEntry);
            }
        } else {
            // Notification privée : seul le destinataire peut la marquer
            if (!notif.getUsername().equals(username)) {
                throw new RuntimeException("User not authorized to modify this notification");
            }
            notif.setReadValue(true);
            notificationRepository.save(notif);
        }
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

        List<UserNotificationRead> readGlobalNotifs = userNotificationReadRepository.findAll()
                .stream()
                .filter(r -> r.getUsername().equals(username))
                .toList();

        Set<Long> readGlobalNotifIds = readGlobalNotifs.stream()
                .map(r -> r.getNotification().getId())
                .collect(Collectors.toSet());

        // filtrer les notifs globales non lues
        List<Notification> unreadGlobalNotifs = globalNotifs.stream()
                .filter(n -> !readGlobalNotifIds.contains(n.getId()))
                .toList();

        List<Notification> all = new ArrayList<>();
        all.addAll(userNotifs);
        all.addAll(unreadGlobalNotifs);

        return all.stream()
                .map(NotificationMapper::fromEntity)
                .toList();
    }



}

