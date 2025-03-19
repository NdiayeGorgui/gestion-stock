package com.gogo.delivered_command_service.mapper;

import com.gogo.base_domaine_service.event.EventStatus;
import com.gogo.base_domaine_service.event.OrderEventDto;
import com.gogo.delivered_command_service.model.Delivered;

import java.time.LocalDateTime;

public class DeliveredMapper {
    public static Delivered mapToDelivered(OrderEventDto orderEventDto){
        return new Delivered(
                null,
                orderEventDto.getId(),
                orderEventDto.getPaymentId(),
                orderEventDto.getCustomerEventDto().getCustomerIdEvent(),
                orderEventDto.getCustomerEventDto().getName(),
                orderEventDto.getCustomerEventDto().getEmail(),
                EventStatus.SHIPPED.name(),
                "Order Shipped",
                LocalDateTime.now()
        );
    }
}
