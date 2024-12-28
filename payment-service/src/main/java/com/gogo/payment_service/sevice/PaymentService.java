package com.gogo.payment_service.sevice;


import com.gogo.base_domaine_service.dto.Payment;


import com.gogo.base_domaine_service.event.OrderEventDto;
import com.gogo.payment_service.kafka.BillConsumer;
import com.gogo.payment_service.kafka.PaymentProducer;
import com.gogo.payment_service.model.Bill;
import com.gogo.payment_service.model.PaymentModel;
import com.gogo.payment_service.repository.BillRepository;
import com.gogo.payment_service.repository.PaymentRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PaymentService {
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private BillRepository billRepository;
    @Autowired
    private PaymentProducer paymentProducer;

    @Autowired
    private BillConsumer billConsumer;





    public void savePayment(PaymentModel paymentModel){
        paymentRepository.save(paymentModel);
    }

    public void saveAndSendPayment(Payment payment){
        PaymentModel savedPayment=new PaymentModel();
        OrderEventDto orderEventDto=new OrderEventDto();

        Bill bill=billRepository.findByOrderRef(payment.getOrderIdEvent());
        double price= bill.getPrice();
        double discount= bill.getDiscount();
        int quantity= bill.getQuantity();

        savedPayment.setPaymentIdEvent(UUID.randomUUID().toString());
        savedPayment.setPaymentStatus("COMPLETED");
        savedPayment.setPaymentMode(payment.getPaymentMode());
        savedPayment.setAmount(price*quantity-(discount));
        savedPayment.setTimeStamp(LocalDateTime.now());
        savedPayment.setOrderIdEvent(payment.getOrderIdEvent());

        this.savePayment(savedPayment);
        billRepository.updateTheBillStatus(savedPayment.getOrderIdEvent(), savedPayment.getPaymentStatus());

        orderEventDto.setId(payment.getOrderIdEvent());
        orderEventDto.setStatus(savedPayment.getPaymentStatus());
        paymentProducer.sendMessage(orderEventDto);




    }

}
