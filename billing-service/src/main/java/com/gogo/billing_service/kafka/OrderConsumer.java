package com.gogo.billing_service.kafka;

import com.gogo.base_domaine_service.dto.Order;
import com.gogo.base_domaine_service.event.OrderEventDto;
import com.gogo.base_domaine_service.event.PaymentEvent;
import com.gogo.base_domaine_service.event.ProductEventDto;
import com.gogo.billing_service.Repository.BillRepository;
import com.gogo.billing_service.model.Bill;
import com.gogo.billing_service.service.BillingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

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
        Bill bill=new Bill();
        if(event.getStatus().equalsIgnoreCase("PENDING")){

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

            boolean billExist = billRepository.existsByOrderRefAndStatus(event.getId(), event.getStatus());


            if (billExist) {
                event.setStatus("CREATED");
                billingService.updateBillStatus(event.getProductEventDto().getProductIdEvent(), event.getStatus());
                LOGGER.info("Bill  event with created status sent to Order service => {}", event);
                billProducer.sendMessage(event);

               /* PaymentEvent paymentEvent=new PaymentEvent();
                paymentEvent.setPaymentIdEvent(UUID.randomUUID().toString());
                paymentEvent.setStatus("PENDING");
                paymentEvent.setMessage("Creating payment");
                paymentEvent.setAmount((bill.getQuantity()*bill.getPrice())-(bill.getDiscount()));
                Order order=new Order();
                order.setOrderId(bill.getOrderRef());
                order.setQty(bill.getQuantity());
                order.setPrice(bill.getPrice());
                paymentEvent.setOrder(order);
                paymentProducer.sendMessage(paymentEvent);*/

            }else {
                event.setStatus("FAILED");
               // event.setMessage("Bill status is in failed state");
                LOGGER.info(String.format("Bill event with failed status sent to Order service => %s", event));
                billProducer.sendMessage(event);
            }
        }
        if(event.getStatus().equalsIgnoreCase("CANCELLING")){
            Bill bill1=billingService.findByOrderRef(event.getId());

            ProductEventDto productEventDto=new ProductEventDto();
            if(bill1!=null && bill1.getStatus().equalsIgnoreCase("CREATED")){
                productEventDto.setQty(bill1.getQuantity());
                productEventDto.setProductIdEvent(bill1.getProductIdEvent());

                event.setProductEventDto(productEventDto);
                event.setStatus("CANCELED");

            billingService.updateTheBillStatus(event.getId(), event.getStatus());
            billProducer.sendMessage(event);
            }
        }
        LOGGER.info(String.format("Order event received in billing service => %s", event));
    }
}
