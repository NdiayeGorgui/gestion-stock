package com.gogo.notification_service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.gogo.notification_service.dto.NotificationDto;
import com.gogo.notification_service.mapper.NotificationMapper;
import com.gogo.notification_service.model.Notification;
import com.gogo.notification_service.model.UserNotificationRead;
import com.gogo.notification_service.repository.NotificationRepository;
import com.gogo.notification_service.repository.UserNotificationReadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
@EnableScheduling
@Service
public class NotificationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationService.class);
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

    @Scheduled(cron = "0 */5 * * * ?") // Toutes les 5 minutes
    public void archiveOldGlobalNotification() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(7);

        LOGGER.info("Début de l’archivage des notifications globales créées avant {}", threshold);

        List<Notification> oldNotifications = notificationRepository
                .findAllByUsernameAndArchivedFalseAndCreatedDateBefore("allusers", threshold);

        if (oldNotifications.isEmpty()) {
            LOGGER.info("Aucune notification globale à archiver.");
        } else {
            oldNotifications.forEach(notif -> notif.setArchived(true));
            notificationRepository.saveAll(oldNotifications);
            LOGGER.info("{} notifications globales archivées avec succès à {}", oldNotifications.size(), LocalDateTime.now());
        }
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

