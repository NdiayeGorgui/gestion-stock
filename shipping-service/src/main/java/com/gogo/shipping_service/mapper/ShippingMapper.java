package com.gogo.shipping_service.mapper;

import com.gogo.base_domaine_service.event.EventStatus;
import com.gogo.base_domaine_service.event.OrderEventDto;
import com.gogo.shipping_service.model.Ship;

import java.time.LocalDateTime;

public class ShippingMapper {
    public static Ship mapToShip(OrderEventDto orderEventDto){
        return new Ship(
                null,
                orderEventDto.getId(),
                orderEventDto.getPaymentId(),
                orderEventDto.getCustomerEventDto().getCustomerIdEvent(),
                orderEventDto.getCustomerEventDto().getName(),
                orderEventDto.getCustomerEventDto().getEmail(),
                EventStatus.SHIPPING.name(),
                "Order Shipping status",
                LocalDateTime.now()
        );
    }
}
