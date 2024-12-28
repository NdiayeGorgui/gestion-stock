package com.gogo.payment_service.kafka;

import com.gogo.base_domaine_service.event.OrderEventDto;
import com.gogo.base_domaine_service.event.PaymentEvent;
import com.gogo.payment_service.model.Bill;
import com.gogo.payment_service.repository.BillRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class BillConsumer {
    @Autowired
    private BillRepository billRepository;
    private static final Logger LOGGER = LoggerFactory.getLogger(BillConsumer.class);
    @KafkaListener(
            topics = "${spring.kafka.topic.billing.name}"
            ,groupId = "${spring.kafka.consumer.group-id}"
    )

    public void billConsumer(OrderEventDto event){
        if(event.getStatus().equalsIgnoreCase("CREATED")){
            Bill bill=new Bill();
            bill.setCustomerIdEvent(event.getCustomerEventDto().getCustomerIdEvent());
            bill.setCustomerName(event.getCustomerEventDto().getName());
            bill.setCustomerPhone(event.getCustomerEventDto().getPhone());

            bill.setProductIdEvent(event.getProductEventDto().getProductIdEvent());
            bill.setProductName(event.getProductEventDto().getName());

            bill.setPrice(event.getProductEventDto().getPrice());
            bill.setQuantity(event.getProductItemEventDto().getQty());
            bill.setDiscount(event.getProductItemEventDto().getDiscount());
            bill.setStatus(event.getStatus());

            bill.setOrderRef(event.getId());
            bill.setBillingDate(LocalDateTime.now());
            billRepository.save(bill);

        }

        LOGGER.info("Order event received in billing service => {}", event);

    }
}
