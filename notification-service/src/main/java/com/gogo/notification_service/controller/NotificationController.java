package com.gogo.notification_service.controller;

import com.gogo.notification_service.dto.NotificationDto;
import com.gogo.notification_service.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/notifications/test-user")
    public String getUsername(@RequestHeader("X-Username") String username) {
        System.out.println("🔐 Received  via Gateway : " + username);
        return "User : " + username;
    }

    @GetMapping("/notifications")
    public List<NotificationDto> getUserAndGlobal(@RequestHeader("X-Username") String username) {
        if (username == null || username.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing username");
        }
        return notificationService.getUserAndGlobalNotifications(username);
    }

    @PutMapping("/notifications/{id}")
    public void markNotificationAsRead( @RequestHeader("X-Username") String username, @PathVariable("id") Long id) {
        notificationService.markAsRead(id, username);
    }

}

