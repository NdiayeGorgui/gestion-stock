package com.gogo.notification_service;

import com.gogo.notification_service.model.Notification;
import com.gogo.notification_service.repository.NotificationRepository;
import com.gogo.notification_service.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class NotificationServiceTest {

    private NotificationRepository notificationRepository;
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationRepository = mock(NotificationRepository.class);
        notificationService = new NotificationService(notificationRepository);
    }

    @Test
    void archiveOldGlobalNotification_shouldArchiveOldNotifications() {
        // Arrange
        Notification oldNotif1 = new Notification();
        oldNotif1.setId(1L);
        oldNotif1.setUsername("allusers");
        oldNotif1.setArchived(false);
        oldNotif1.setCreatedDate(LocalDateTime.now().minusDays(8));

        Notification oldNotif2 = new Notification();
        oldNotif2.setId(2L);
        oldNotif2.setUsername("allusers");
        oldNotif2.setArchived(false);
        oldNotif2.setCreatedDate(LocalDateTime.now().minusDays(10));

        List<Notification> oldNotifications = Arrays.asList(oldNotif1, oldNotif2);

        when(notificationRepository.findAllByUsernameAndArchivedFalseAndCreatedDateBefore(
                eq("allusers"),
                any(LocalDateTime.class)
        )).thenReturn(oldNotifications);

        // Act
        notificationService.archiveOldGlobalNotification();

        // Assert
        ArgumentCaptor<List<Notification>> captor = ArgumentCaptor.forClass(List.class);
        verify(notificationRepository).saveAll(captor.capture());

        List<Notification> saved = captor.getValue();
        assertEquals(2, saved.size());
        assertTrue(saved.stream().allMatch(Notification::isArchived));
    }
}

