package com.gogo.notification_service.mapper;

import com.gogo.notification_service.dto.NotificationDto;
import com.gogo.notification_service.model.Notification;

public class NotificationMapper {

    public static NotificationDto fromEntity(Notification notification) {
        NotificationDto dto = new NotificationDto();
        dto.setId(notification.getId());
        dto.setMessage(notification.getMessage());
        dto.setReadValue(notification.isReadValue()); // ✅ ici le mapping est bon
        return dto;
    }

}
