package com.gogo.notification_service.service;

import com.gogo.notification_service.dto.NotificationDto;
import com.gogo.notification_service.mapper.NotificationMapper;
import com.gogo.notification_service.model.Notification;
import com.gogo.notification_service.model.UserNotificationRead;
import com.gogo.notification_service.repository.NotificationRepository;
import com.gogo.notification_service.repository.UserNotificationReadRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    @Scheduled(cron = "0 0 0 * * ?") // Tous les jours à minuit
    public void archiveOldGlobalNotification() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(7);  //vieille de 7 jours

        // Archiver les notifications globales
        List<Notification> globalNotifications = notificationRepository
                .findAllByUsernameAndArchivedFalseAndCreatedDateBefore("allusers", threshold);

        // Archiver les notifications utilisateur
        List<Notification> userNotifications = notificationRepository
                .findAllByUsernameNotAndArchivedFalseAndCreatedDateBefore("allusers", threshold);

        globalNotifications.forEach(n -> n.setArchived(true));
        userNotifications.forEach(n -> n.setArchived(true));

        List<Notification> allToArchive = new ArrayList<>();
        allToArchive.addAll(globalNotifications);
        allToArchive.addAll(userNotifications);

        notificationRepository.saveAll(allToArchive);

        System.out.println("🔔 Archivage terminé pour " + allToArchive.size() + " notifications.");
    }

    public List<NotificationDto> getUserAndGlobalNotifications(String username) {
        // Récupère uniquement les notifications non archivées de l'utilisateur
        List<Notification> userNotifs = notificationRepository
                .findByUsernameAndArchivedFalseOrderByIdDesc(username);

        // Récupère uniquement les notifications globales non archivées
        List<Notification> globalNotifs = notificationRepository
                .findByUsernameAndArchivedFalse("allusers");

        // Liste des notifications globales déjà lues par cet utilisateur
        List<UserNotificationRead> readGlobalNotifs = userNotificationReadRepository.findAll()
                .stream()
                .filter(r -> r.getUsername().equals(username))
                .toList();

        Set<Long> readGlobalNotifIds = readGlobalNotifs.stream()
                .map(r -> r.getNotification().getId())
                .collect(Collectors.toSet());

        // On filtre les globales pour ne garder que celles que l'utilisateur n’a pas encore lues
        List<Notification> unreadGlobalNotifs = globalNotifs.stream()
                .filter(n -> !readGlobalNotifIds.contains(n.getId()))
                .toList();

        // On fusionne les notifications utilisateur + globales non lues
        List<Notification> all = new ArrayList<>();
        all.addAll(userNotifs);
        all.addAll(unreadGlobalNotifs);

        return all.stream()
                .map(NotificationMapper::fromEntity)
                .toList();
    }
}

