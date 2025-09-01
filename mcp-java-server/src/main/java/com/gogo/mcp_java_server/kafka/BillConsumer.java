package com.gogo.mcp_java_server.kafka;

import com.gogo.base_domaine_service.event.EventStatus;
import com.gogo.base_domaine_service.event.OrderEventDto;

import com.gogo.mcp_java_server.mapper.MpcMapper;
import com.gogo.mcp_java_server.model.Bill;
import com.gogo.mcp_java_server.service.StockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BillConsumer {

    @Autowired
    private StockService stockService;

    private static final Logger LOGGER = LoggerFactory.getLogger(BillConsumer.class);

    @KafkaListener(
            topics = "${spring.kafka.topic.billing.name}"
            , groupId = "${spring.kafka.consumer.bill.group-id}"
    )
    public void billConsumer(OrderEventDto event) {
        LOGGER.info("💰 Mpc service received event => {}", event);

        // Création de la facture
        if (EventStatus.CREATED.name().equalsIgnoreCase(event.getStatus())) {
            List<Bill> bills = MpcMapper.mapToBills(event);
            for (Bill bill : bills) {
                stockService.saveBill(bill);
                LOGGER.info("✅ Bill created for product {} in order {}", bill.getProductId(), event.getId());
            }
        }

        // Annulation de la facture
        else if (EventStatus.CANCELED.name().equalsIgnoreCase(event.getStatus())) {
            if (event.getProductItemEventDtos() != null) {
                for (var item : event.getProductItemEventDtos()) {
                    Bill bill = stockService.findByOrderIdAndProductId(event.getId(), item.getProductIdEvent());
                    if (bill != null && EventStatus.CREATED.name().equalsIgnoreCase(bill.getStatus())) {
                        stockService.updateTheBillStatus(bill.getOrderRef(), EventStatus.CANCELED.name());
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
