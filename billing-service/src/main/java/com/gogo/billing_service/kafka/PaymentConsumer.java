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
public class PaymentConsumer {
    @Autowired
    BillProducer billProducer;
    @Autowired
    BillingService billingService;

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentConsumer.class);

    @KafkaListener(
            topics = "${spring.kafka.topic.payment.name}"
            , groupId = "${spring.kafka.consumer.payment.group-id}"
    )
    public void consumeProductStatus(OrderEventDto event) {

        if (event.getStatus().equalsIgnoreCase(EventStatus.COMPLETED.name())) {

            event.setStatus(EventStatus.COMPLETED.name());
           // billingService.updateTheBillStatus(event.getId(), event.getStatus());
            billingService.updateAllBillCustomerStatus(event.getCustomerEventDto().getCustomerIdEvent(), event.getStatus());

            LOGGER.info("Product Update event with Created status sent to Order service => {}", event);
            //  billProducer.sendMessage(event);
        }
    }
}

