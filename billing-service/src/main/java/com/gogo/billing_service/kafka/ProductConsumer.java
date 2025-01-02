package com.gogo.billing_service.kafka;

import com.gogo.base_domaine_service.event.EventStatus;
import com.gogo.base_domaine_service.event.OrderEventDto;
import com.gogo.billing_service.service.BillingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class ProductConsumer {
    @Autowired
    BillProducer billProducer;
    @Autowired
    BillingService billingService;

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductConsumer.class);

    @KafkaListener(
            topics = "${spring.kafka.topic.quantity.update.name}"
            , groupId = "${spring.kafka.update.bill.consumer.group-id}"
    )
    public void consumeProductStatus(OrderEventDto event) {

        if (event.getStatus().equalsIgnoreCase(EventStatus.MODIFIED.name())) {

            event.setStatus(EventStatus.CREATED.name());
            billingService.updateBillStatus(event.getProductEventDto().getProductIdEvent(), event.getStatus());

            LOGGER.info("Product Update event with Created status sent to Order service => {}", event);

            //  billProducer.sendMessage(event);
        }
    }
}

