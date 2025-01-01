package com.gogo.payment_service.mapper;

import com.gogo.base_domaine_service.event.EventStatus;
import com.gogo.base_domaine_service.event.OrderEventDto;
import com.gogo.payment_service.model.Bill;

import java.time.LocalDateTime;

public class PaymentMapper {
    public static Bill mapToBill(OrderEventDto orderEventDto){
        Bill bill=new Bill(
                null,
                LocalDateTime.now(),
                orderEventDto.getCustomerEventDto().getCustomerIdEvent(),
                orderEventDto.getCustomerEventDto().getName(),
                orderEventDto.getCustomerEventDto().getPhone(),
                orderEventDto.getId(),
                orderEventDto.getProductEventDto().getProductIdEvent(),
                orderEventDto.getProductEventDto().getName(),
                orderEventDto.getProductItemEventDto().getQty(),
                orderEventDto.getProductEventDto().getPrice(),
                orderEventDto.getProductItemEventDto().getDiscount(),
                orderEventDto.getStatus()
        );
        return bill;
    }
}
