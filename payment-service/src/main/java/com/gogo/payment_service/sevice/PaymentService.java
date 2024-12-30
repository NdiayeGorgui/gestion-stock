package com.gogo.payment_service.sevice;


import com.gogo.base_domaine_service.dto.Payment;

import com.gogo.base_domaine_service.event.CustomerEventDto;
import com.gogo.base_domaine_service.event.OrderEventDto;
import com.gogo.payment_service.kafka.PaymentProducer;
import com.gogo.payment_service.model.Bill;
import com.gogo.payment_service.model.PaymentModel;
import com.gogo.payment_service.repository.BillRepository;
import com.gogo.payment_service.repository.PaymentRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private BillRepository billRepository;
    @Autowired
    private PaymentProducer paymentProducer;


    public void savePayment(PaymentModel paymentModel){
        paymentRepository.save(paymentModel);
    }


    public void saveAndSendPayment(Payment payment){
        PaymentModel savedPayment=new PaymentModel();
        OrderEventDto orderEventDto=new OrderEventDto();

        double amount= this.getAmount(payment.getCustomerIdEvent(),"CREATED");
        double discount=this.getDiscount(payment.getCustomerIdEvent(),"CREATED");

       // List<Bill> bills=this.getBillsByCustomer(payment.getCustomerIdEvent(),"CREATED");

        savedPayment.setPaymentIdEvent(UUID.randomUUID().toString());
        savedPayment.setPaymentStatus("COMPLETED");
        savedPayment.setPaymentMode(payment.getPaymentMode());
        savedPayment.setCustomerIdEvent(payment.getCustomerIdEvent());
        savedPayment.setAmount((amount-discount));
        savedPayment.setTimeStamp(LocalDateTime.now());
        this.savePayment(savedPayment);

        billRepository.updateAllBillCustomerStatus(savedPayment.getCustomerIdEvent(),savedPayment.getPaymentStatus());

        CustomerEventDto customerEventDto=new CustomerEventDto();
        customerEventDto.setCustomerIdEvent(payment.getCustomerIdEvent());

        orderEventDto.setCustomerEventDto(customerEventDto);
        orderEventDto.setStatus(savedPayment.getPaymentStatus());
        paymentProducer.sendMessage(orderEventDto);

    }
    public List<Bill> getBillsByCustomer(String customerIdEvent,String status){
        return billRepository.findByCustomerIdEventAndStatus(customerIdEvent,status);
    }

    public  double getAmount(String customerIdEvent,String status){
        List<Bill> customerBills=this.getBillsByCustomer(customerIdEvent,status);
        return customerBills.stream()
                .filter(bill -> bill.getCustomerIdEvent().equalsIgnoreCase(customerIdEvent))
                .filter(bill -> bill.getStatus().equalsIgnoreCase(status))
                .map(bill -> (bill.getPrice()*bill.getQuantity()))
                .mapToDouble(i->i).sum();
    }

    public  double getDiscount(String customerIdEvent,String status){
        List<Bill> customerBills=this.getBillsByCustomer(customerIdEvent,status);
        return customerBills.stream()
                .filter(bill -> bill.getCustomerIdEvent().equalsIgnoreCase(customerIdEvent))
                .filter(bill -> bill.getStatus().equalsIgnoreCase(status))
                .map(Bill::getDiscount)
                .mapToDouble(i->i).sum();
    }

}
