package com.gogo.billing_service.mapper;

import com.gogo.base_domaine_service.event.OrderEventDto;
import com.gogo.billing_service.model.Bill;

import java.time.LocalDateTime;

public class BillMapper {

    public static Bill mapToBill(OrderEventDto orderEventDto){
        return new Bill(
                null,
                LocalDateTime.now(),
                orderEventDto.getCustomerEventDto().getCustomerIdEvent(),
                orderEventDto.getCustomerEventDto().getName(),
                orderEventDto.getCustomerEventDto().getPhone(),
                orderEventDto.getCustomerEventDto().getEmail(),
                orderEventDto.getId(),
                orderEventDto.getPaymentId(),
                orderEventDto.getProductEventDto().getProductIdEvent(),
                orderEventDto.getProductEventDto().getName(),
                orderEventDto.getProductItemEventDto().getQty(),
                orderEventDto.getProductEventDto().getPrice(),
                orderEventDto.getProductItemEventDto().getDiscount(),
                orderEventDto.getStatus()
        );
    }
}
