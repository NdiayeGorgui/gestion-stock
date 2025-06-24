package com.gogo.billing_service.mapper;

import com.gogo.base_domaine_service.event.OrderEventDto;
import com.gogo.base_domaine_service.event.ProductItemEventDto;
import com.gogo.billing_service.model.Bill;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BillMapper {

    public static List<Bill> mapToBills(OrderEventDto event) {
        if (event.getProductItemEventDtos() == null || event.getCustomerEventDto() == null) {
            throw new IllegalArgumentException("ProductItemEventDtos or CustomerEventDto is null");
        }

        List<Bill> bills = new ArrayList<>();

        for (ProductItemEventDto item : event.getProductItemEventDtos()) {
            Bill bill = new Bill(
                    null,
                    LocalDateTime.now(),
                    event.getCustomerEventDto().getCustomerIdEvent(),
                    event.getCustomerEventDto().getName(),
                    event.getCustomerEventDto().getPhone(),
                    event.getCustomerEventDto().getEmail(),
                    event.getId(),                     // orderRef
                    // event.getPaymentId(),              // paymentRef
                    item.getProductIdEvent(),          // ✅ product ID
                    item.getProductName(),                    // ✅ product name (doit être prérempli côté OrderService)
                    item.getQty(),
                    item.getPrice(),
                    item.getDiscount(),
                    event.getStatus()
            );
            bills.add(bill);
        }

        return bills;
    }


}
