package com.gogo.payment_service.mapper;

import com.gogo.base_domaine_service.dto.Payment;
import com.gogo.base_domaine_service.event.EventStatus;
import com.gogo.base_domaine_service.event.OrderEventDto;
import com.gogo.payment_service.model.Bill;
import com.gogo.payment_service.model.PaymentModel;

import java.time.LocalDateTime;
import java.util.UUID;

public class PaymentMapper {
    public static Bill mapToBill(OrderEventDto orderEventDto){
        return new Bill(
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
    }

    public static PaymentModel mapToPaymentModel(Payment payment,double amount, double discount){
        return new PaymentModel(
                null,
                UUID.randomUUID().toString(),
                payment.getCustomerIdEvent(),
                payment.getPaymentMode(),
                (amount-discount),
                LocalDateTime.now(),
                EventStatus.COMPLETED.name()
        );
    }
}
