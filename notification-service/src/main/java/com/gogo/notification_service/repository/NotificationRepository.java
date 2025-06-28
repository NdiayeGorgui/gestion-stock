package com.gogo.notification_service.repository;

import com.gogo.notification_service.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Récupérer les notifications par utilisateur
    List<Notification> findByUsernameOrderByIdDesc(String username);

    //Ce query method va permettre de vérifier rapidement si une notification globale identique existe déjà (non lue et non archivée).
    boolean existsByMessageAndUsernameAndReadValueIsFalseAndArchivedIsFalse(String message, String username);

    List<Notification> findAllByUsernameAndArchivedFalseAndCreatedDateBefore(String username, LocalDateTime date);

    List<Notification> findAllByUsernameNotAndArchivedFalseAndCreatedDateBefore(String username, LocalDateTime date);

    // Pour les notifications de l'utilisateur
    List<Notification> findByUsernameAndArchivedFalseOrderByIdDesc(String username);

    // Pour les globales
    List<Notification> findByUsernameAndArchivedFalse(String username);

    boolean existsByProductKeyAndTypeAndArchivedIsFalseAndReadValueIsFalse(String productKey, String type);

    @Modifying
    @org.springframework.transaction.annotation.Transactional
    @Query("UPDATE Notification n SET n.archived = true WHERE n.productKey IN (:keys) AND n.archived = false")
    void archiveByProductKeyIn(@Param("keys") List<String> keys);


}

