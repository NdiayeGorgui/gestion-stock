package com.gogo.payment_service.kafka;

import com.gogo.base_domaine_service.event.EventStatus;
import com.gogo.base_domaine_service.event.OrderEventDto;
import com.gogo.payment_service.mapper.PaymentMapper;
import com.gogo.payment_service.model.Bill;
import com.gogo.payment_service.sevice.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BillConsumer {

    @Autowired
    private PaymentService paymentService;

    private static final Logger LOGGER = LoggerFactory.getLogger(BillConsumer.class);

    @KafkaListener(
            topics = "${spring.kafka.topic.billing.name}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void billConsumer(OrderEventDto event) {
        LOGGER.info("💰 Payment service received event => {}", event);

        // Création de la facture
        if (EventStatus.CREATED.name().equalsIgnoreCase(event.getStatus())) {
            List<Bill> bills = PaymentMapper.mapToBills(event);
            for (Bill bill : bills) {
                paymentService.saveBill(bill);
                LOGGER.info("✅ Bill created for product {} in order {}", bill.getProductIdEvent(), event.getId());
            }
        }

        // Annulation de la facture
        else if (EventStatus.CANCELED.name().equalsIgnoreCase(event.getStatus())) {
            if (event.getProductItemEventDtos() != null) {
                for (var item : event.getProductItemEventDtos()) {
                    Bill bill = paymentService.findByOrderIdAndProductIdEvent(event.getId(), item.getProductIdEvent());
                    if (bill != null && EventStatus.CREATED.name().equalsIgnoreCase(bill.getStatus())) {
                        paymentService.updateTheBillStatus(bill.getOrderRef(), EventStatus.CANCELED.name());
                        LOGGER.info("❌ Bill canceled for product {} in order {}", item.getProductIdEvent(), event.getId());
                    } else {
                        LOGGER.warn("⚠️ Bill not found or already canceled for product {} in order {}", item.getProductIdEvent(), event.getId());
                    }
                }
            } else {
                LOGGER.warn("⚠️ No product items found to cancel in event: {}", event.getId());
            }
        }
    }
}
