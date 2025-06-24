package com.gogo.delivered_query_service.mapper;

import com.gogo.base_domaine_service.event.OrderEventDto;
import com.gogo.base_domaine_service.event.ProductItemEventDto;
import com.gogo.delivered_query_service.dto.DeliveredResponseDto;
import com.gogo.delivered_query_service.dto.ProductItemDto;
import com.gogo.delivered_query_service.model.Bill;
import com.gogo.delivered_query_service.model.Delivered;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DeliveredMapper {
    public static DeliveredResponseDto toDto(Delivered delivered) {
        DeliveredResponseDto dto = new DeliveredResponseDto();
        dto.setOrderId(delivered.getOrderId());
       // dto.setPaymentId(delivered.getPaymentId());
        dto.setCustomerName(delivered.getCustomerName());
        dto.setCustomerMail(delivered.getCustomerMail());
        dto.setDeliveryStatus(delivered.getStatus());
        //dto.setDetails(delivered.getDetails());
        dto.setTimeStamp(delivered.getEventTimeStamp());
        // dto.setProducts(...) // si tu veux enrichir via appel à un service externe
        return dto;
    }

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
                    event.getId(),
                   // event.getPaymentId(),
                    item.getProductIdEvent(),
                    item.getProductName(),
                    item.getQty(),
                    item.getPrice(),
                    item.getDiscount(),
                    event.getStatus()
            );
            bills.add(bill);
        }

        return bills;
    }



    public static DeliveredResponseDto mapToDeliveredResponseDto(Delivered delivered, List<Bill> bills) {
        DeliveredResponseDto dto = new DeliveredResponseDto();
        dto.setPaymentId(delivered.getPaymentId());
        dto.setOrderId(delivered.getOrderId());
        dto.setCustomerName(delivered.getCustomerName());
        dto.setCustomerMail(delivered.getCustomerMail());
        // dto.setPaymentMode(payment.getPaymentMode());
        //dto.setAmount(payment.getAmount());
        dto.setDeliveryStatus(delivered.getStatus());
        dto.setTimeStamp(delivered.getEventTimeStamp());

        List<ProductItemDto> items = bills.stream().map(bill -> {
            ProductItemDto item = new ProductItemDto();
            item.setProductId(bill.getProductIdEvent());
            item.setProductName(bill.getProductName());
            item.setQuantity(bill.getQuantity());
            item.setPrice(bill.getPrice());
            item.setDiscount(bill.getDiscount());
            // 🧮 Calcul du montant net par produit et de la taxe (20%)
            double netCal= (bill.getPrice() * bill.getQuantity()) - bill.getDiscount();
            double tax = Math.round(netCal * 0.20 * 100.0) / 100.0;
            item.setTax(tax);
            return item;
        }).collect(Collectors.toList());

        dto.setProducts(items);
        // 🔢 Calculer le subtotal
        double subtotal = items.stream()
                .mapToDouble(i -> i.getPrice() * i.getQuantity())
                .sum();
        subtotal = Math.round(subtotal * 100.0) / 100.0;

        // 🔻 Calculer le discount total (même logique que OrderService)
        double totalDiscount = items.stream()
                .mapToDouble(i -> {
                    double total = i.getPrice() * i.getQuantity();
                    if (total < 100) return 0;
                    else if (total < 200) return 0.005 * total;
                    else return 0.01 * total;
                }).sum();
        totalDiscount = Math.round(totalDiscount * 100.0) / 100.0;

        double netAmount = subtotal - totalDiscount;
        netAmount = Math.round(netAmount * 100.0) / 100.0;

        double totalTax = netAmount * 0.2;
        totalTax = Math.round(totalTax * 100.0) / 100.0;

        double ttc = netAmount + totalTax;
        ttc = Math.round(ttc * 100.0) / 100.0;

        dto.setAmount(ttc);
        dto.setTotalDiscount(totalDiscount);
        dto.setTotalTax(totalTax);

        return dto;
    }


}

