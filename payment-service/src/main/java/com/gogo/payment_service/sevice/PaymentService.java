package com.gogo.payment_service.sevice;


import com.gogo.base_domaine_service.dto.*;

import com.gogo.base_domaine_service.event.*;

import com.gogo.payment_service.kafka.PaymentProducer;
import com.gogo.payment_service.mapper.PaymentMapper;
import com.gogo.payment_service.model.Bill;
import com.gogo.payment_service.model.PaymentModel;
import com.gogo.payment_service.repository.BillRepository;
import com.gogo.payment_service.repository.PaymentRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PaymentService {
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private BillRepository billRepository;
    @Autowired
    private PaymentProducer paymentProducer;

    public Bill getBill(String customerId, String status) {
        return billRepository.findByCustomerIdEventAndStatus(customerId, status)
                             .stream()
                             .findFirst()
                             .orElse(null);
    }


    public void savePayment(PaymentModel paymentModel){
        paymentRepository.save(paymentModel);
    }

    public void saveBill(Bill bill){
        billRepository.save(bill);
    }

    public void saveAndSendPayment(Payment payment){

        OrderEventDto orderEventDto=new OrderEventDto();

        double amount= this.getAmount(payment.getCustomerIdEvent(), EventStatus.CREATED.name());
        double discount=this.getDiscount(payment.getCustomerIdEvent(),EventStatus.CREATED.name());
        double ttc=amount*1.2; // la taxe est de 20%

        PaymentModel savedPayment= PaymentMapper.mapToPaymentModel(payment,ttc,discount);

        Bill billCreated = this.getBill(payment.getCustomerIdEvent(), EventStatus.CREATED.name());
        String customerNameCreated= billCreated.getCustomerName();
        String customerMailCreated=billCreated.getCustomerMail();
        savedPayment.setCustomerName(customerNameCreated);
        savedPayment.setCustomerMail(customerMailCreated);
        savedPayment.setOrderId(billCreated.getOrderId());

        this.savePayment(savedPayment);

        billRepository.updateAllBillCustomerStatus(savedPayment.getCustomerIdEvent(),EventStatus.COMPLETED.name());

  
        CustomerEventDto customerEventDto=new CustomerEventDto();
        customerEventDto.setCustomerIdEvent(payment.getCustomerIdEvent());
        customerEventDto.setName(customerNameCreated);
        customerEventDto.setEmail(customerMailCreated);

        orderEventDto.setCustomerEventDto(customerEventDto);
        orderEventDto.setStatus(savedPayment.getPaymentStatus());

        orderEventDto.setPaymentId(billCreated.getOrderId());

        paymentProducer.sendMessage(orderEventDto);

    }

    public void cancelAndSendOrder(Payment payment){

        OrderEventDto orderEventDto=new OrderEventDto();
        // find all orders concerned

        billRepository.updateAllBillCustomerStatus(payment.getCustomerIdEvent(),payment.getPaymentStatus());



        Bill bill = this.getBill(payment.getCustomerIdEvent(), EventStatus.CREATED.name());
        String customerName= bill.getCustomerName();
        String customerMail=bill.getCustomerMail();
        CustomerEventDto customerEventDto=new CustomerEventDto();
        customerEventDto.setCustomerIdEvent(payment.getCustomerIdEvent());
        customerEventDto.setName(customerName);
        customerEventDto.setEmail(customerMail);

        orderEventDto.setCustomerEventDto(customerEventDto);
        orderEventDto.setStatus(EventStatus.REMOVED.name());
        orderEventDto.setPaymentId(orderEventDto.getPaymentId());//todo
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

    public void updateTheBillStatus(String orderIdEvent, String status){
        billRepository.updateTheBillStatus(orderIdEvent, status);
    }

    public boolean billExist(String orderRef,String status){
        return billRepository.existsByOrderRefAndStatus(orderRef,status);
    }

    public Bill findByOrderIdEvent(String orderIdEvent){
        return billRepository.findByOrderRef(orderIdEvent);
    }

    public List<Bill> findByCustomer(String customerIdEvent){
        List<Bill> customerBills=billRepository.findByCustomerIdEvent(customerIdEvent);
        return customerBills.stream()
                .filter(bill -> bill.getCustomerIdEvent().equalsIgnoreCase(customerIdEvent))
                .collect(Collectors.toList());
    }

    public List<PaymentModel> findAllPayments(){
        return paymentRepository.findAll();
    }

    public PaymentModel findPaymentById(String paymentIdEvent){
        return paymentRepository.findByPaymentIdEvent(paymentIdEvent);
    }
    public  List<Bill> getBills(){
       return billRepository.findAll();
    }
}
