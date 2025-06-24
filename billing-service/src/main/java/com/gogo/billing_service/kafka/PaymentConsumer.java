package com.gogo.billing_service.kafka;

import com.gogo.base_domaine_service.event.EventStatus;
import com.gogo.base_domaine_service.event.OrderEventDto;
import com.gogo.billing_service.model.Bill;
import com.gogo.billing_service.service.BillingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentConsumer {
    @Autowired
    private BillProducer billProducer;
    @Autowired
    private BillingService billingService;

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentConsumer.class);

    @KafkaListener(
            topics = "${spring.kafka.topic.payment.name}"
            , groupId = "${spring.kafka.consumer.payment.group-id}"
    )
    public void consumeProductStatus(OrderEventDto event) {

        if (event.getStatus().equalsIgnoreCase(EventStatus.COMPLETED.name())) {

            event.setStatus(EventStatus.COMPLETED.name());
           // billingService.updateTheBillStatus(event.getId(), event.getStatus());
            List<Bill> billList=billingService.billList(event.getCustomerEventDto().getCustomerIdEvent(),EventStatus.CREATED.name());
            for (Bill bill:billList){
                if (bill.getStatus().equalsIgnoreCase(EventStatus.CREATED.name())){
                    bill.setStatus(event.getStatus());
                    billingService.saveBill(bill);
                 //   billingService.updateAllBillCustomerStatus(bill.getCustomerIdEvent(), event.getStatus());
                }
            }


            LOGGER.info("Product Update event with Created status sent to Order service => {}", event);
            //  billProducer.sendMessage(event);
        }
    }
}

