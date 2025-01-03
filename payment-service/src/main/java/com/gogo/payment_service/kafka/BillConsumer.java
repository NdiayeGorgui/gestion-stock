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

@Service
public class BillConsumer {

    @Autowired
    private PaymentService paymentService;
    private static final Logger LOGGER = LoggerFactory.getLogger(BillConsumer.class);

    @KafkaListener(
            topics = "${spring.kafka.topic.billing.name}"
            ,groupId = "${spring.kafka.consumer.group-id}"
    )
    public void billConsumer(OrderEventDto event){
        if(event.getStatus().equalsIgnoreCase(EventStatus.CREATED.name())){
            Bill bill= PaymentMapper.mapToBill(event);
            paymentService.saveBill(bill);
        }
        if(event.getStatus().equalsIgnoreCase(EventStatus.CANCELED.name())){

            Bill bill = paymentService.findByOrderIdEvent(event.getId());
            boolean billExist = paymentService.billExist(bill.getOrderRef(), EventStatus.CREATED.name());
            if(billExist){
                paymentService.updateTheBillStatus(event.getId(), event.getStatus());
            }
        }
        LOGGER.info("Order event received in billing service => {}", event);


    }
}
