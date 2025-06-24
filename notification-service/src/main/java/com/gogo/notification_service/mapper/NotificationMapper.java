package com.gogo.notification_service.mapper;

import com.gogo.notification_service.dto.NotificationDto;
import com.gogo.notification_service.dto.OrderEventForNotificationDto;
import com.gogo.notification_service.dto.ProductEventNotificationDto;
import com.gogo.notification_service.dto.ProductItemNotificationDto;

import com.gogo.notification_service.model.Notification;
import com.gogo.base_domaine_service.event.OrderEventDto;


// ✅ On supprime l'import qui crée la confusion
// ✅ On utilise le nom de classe complet pour éviter le conflit

import com.gogo.base_domaine_service.event.ProductEventDto;

import java.util.List;
import java.util.stream.Collectors;

public class NotificationMapper {

    public static NotificationDto fromEntity(Notification notification) {
        NotificationDto dto = new NotificationDto();
        dto.setId(notification.getId());
        dto.setMessage(notification.getMessage());
        dto.setReadValue(notification.isReadValue());
        dto.setType(notification.getUsername().equals("allusers") ? "global" : "user");
        dto.setProductKey(notification.getProductKey());
        return dto;
    }

    public static OrderEventForNotificationDto mapToLocalOrderEvent(OrderEventDto event) {
        // 🔁 Construire une map des stocks à partir des ProductItemEventDto et du ProductEventDto (unique)
        List<ProductEventNotificationDto> products = event.getProductItemEventDtos().stream()
                .map(item -> {
                    // Si le seul ProductEventDto correspond au produit, on prend le stock
                    // int stock = 0;
                    // ProductEventDto p = event.getProductEventDto();
                    int stock = event.getProductEventDtos().stream()
                            .filter(p -> p.getProductIdEvent().equals(item.getProductIdEvent()))
                            .map(ProductEventDto::getQty)
                            .findFirst()
                            .orElse(0);

                    return new ProductEventNotificationDto(item.getProductIdEvent(), stock);
                }).distinct().toList();

        List<ProductItemNotificationDto> items = event.getProductItemEventDtos().stream()
                .map(item -> {
                    int initialQty = products.stream()
                            .filter(p -> p.getProductIdEvent().equals(item.getProductIdEvent()))
                            .map(ProductEventNotificationDto::getQty)
                            .findFirst()
                            .orElse(0);

                    return new ProductItemNotificationDto(
                            item.getProductIdEvent(),
                            item.getProductName(),
                            item.getQty(),
                            initialQty
                    );
                })
                .collect(Collectors.toList());

        return new OrderEventForNotificationDto(
                event.getId(),
                event.getStatus(),
                event.getUserName(),
                event.getPaymentId(),
                products,
                items
        );
    }


}
