package com.gogo.billing_service.kafka;

import com.gogo.base_domaine_service.event.EventStatus;
import com.gogo.base_domaine_service.event.OrderEventDto;
import com.gogo.base_domaine_service.event.ProductEventDto;
import com.gogo.billing_service.Repository.BillRepository;
import com.gogo.billing_service.mapper.BillMapper;
import com.gogo.billing_service.model.Bill;
import com.gogo.billing_service.service.BillingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;


@Service
public class OrderConsumer {
    @Autowired
   private BillRepository billRepository;
    @Autowired
    private BillProducer billProducer;

    @Autowired
    private PaymentProducer paymentProducer;

    @Autowired
    private BillingService billingService;
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderConsumer.class);

    @KafkaListener(
            topics = "${spring.kafka.topic.name}"
            ,groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumeOrder(OrderEventDto event){
        // save the bill event into the database

        if(event.getStatus().equalsIgnoreCase(EventStatus.PENDING.name())){
            Bill bill= BillMapper.mapToBill(event);
            billRepository.save(bill);

            boolean billExist = billRepository.existsByOrderRefAndStatus(event.getId(), event.getStatus());
            if (billExist) {
                event.setStatus(EventStatus.CREATED.name());
                billingService.updateTheBillStatus(event.getId(), event.getStatus());
                LOGGER.info("Bill  event with created status sent to Order service => {}", event);
                billProducer.sendMessage(event);

            }else {
                event.setStatus(EventStatus.FAILED.name());
               // event.setMessage("Bill status is in failed state");
                LOGGER.info("Bill event with failed status sent to Order service => {}", event);
                billProducer.sendMessage(event);
            }
        }
        if(event.getStatus().equalsIgnoreCase(EventStatus.CANCELLING.name())){
            Bill bill1=billingService.findByOrderRef(event.getId());

            ProductEventDto productEventDto=new ProductEventDto();
            if(bill1!=null && bill1.getStatus().equalsIgnoreCase(EventStatus.CREATED.name())){
                productEventDto.setQty(bill1.getQuantity());
                productEventDto.setProductIdEvent(bill1.getProductIdEvent());

                event.setProductEventDto(productEventDto);
                event.setStatus(EventStatus.CANCELED.name());

            billingService.updateTheBillStatus(event.getId(), event.getStatus());
            billProducer.sendMessage(event);
            }
        }
        LOGGER.info("Order event received in billing service => {}", event);
    }
}
