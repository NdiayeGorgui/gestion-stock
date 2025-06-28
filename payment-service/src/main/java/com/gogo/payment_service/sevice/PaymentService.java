package com.gogo.payment_service.sevice;


import com.gogo.base_domaine_service.dto.Payment;
import com.gogo.base_domaine_service.event.CustomerEventDto;
import com.gogo.base_domaine_service.event.EventStatus;
import com.gogo.base_domaine_service.event.OrderEventDto;
import com.gogo.base_domaine_service.event.ProductItemEventDto;
import com.gogo.payment_service.dto.PaymentResponseDto;
import com.gogo.payment_service.kafka.PaymentProducer;
import com.gogo.payment_service.mapper.PaymentMapper;
import com.gogo.payment_service.model.Bill;
import com.gogo.payment_service.model.PaymentModel;
import com.gogo.payment_service.repository.BillRepository;
import com.gogo.payment_service.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PaymentService {
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private BillRepository billRepository;
    @Autowired
    private PaymentProducer paymentProducer;
    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentService.class);

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

    public void saveAndSendPayment(Payment payment, String username) {
        String orderId = payment.getOrderId();

        // ✅ 1. Vérifier si un paiement a déjà été effectué
        if (paymentRepository.existsByOrderId(orderId)) {
            LOGGER.warn("⚠️ Payment already exists for order: {}", orderId);
            throw new IllegalStateException("⚠️ Payment already exists for order: " + orderId);
        }

        List<Bill> bills = billRepository.findByOrderRef(orderId);
        if (bills == null || bills.isEmpty()) {
            LOGGER.error("❌ No bills found for order: {}", orderId);
            throw new RuntimeException("No bills found for order: " + orderId);
        }

        // 🔢 Calculs
        double subtotal = bills.stream()
                .mapToDouble(b -> b.getPrice() * b.getQuantity())
                .sum();
        double discount = bills.stream()
                .mapToDouble(Bill::getDiscount)
                .sum();
        double ttc = subtotal * 1.2; // TVA 20%

        // ✅ Sauvegarder le paiement
        Bill firstBill = bills.get(0);
        PaymentModel savedPayment = PaymentMapper.mapToPaymentModel(payment, ttc, discount);
        savedPayment.setCustomerIdEvent(firstBill.getCustomerIdEvent());
        savedPayment.setCustomerName(firstBill.getCustomerName());
        savedPayment.setCustomerMail(firstBill.getCustomerMail());
        savedPayment.setOrderId(orderId);

        this.savePayment(savedPayment);


        // ✅ Mettre à jour les factures
        billRepository.updateAllBillOrderStatus(orderId, EventStatus.COMPLETED.name());

        // ✅ Construire l’événement
        CustomerEventDto customerEventDto = new CustomerEventDto(
                firstBill.getCustomerIdEvent(),
                null,
                firstBill.getCustomerName(),
                null,
                firstBill.getCustomerMail(),
                null,
                null,
                null
        );

        List<ProductItemEventDto> productItemEventDtos = bills.stream().map(bill -> {
            ProductItemEventDto dto = new ProductItemEventDto();
            dto.setProductIdEvent(bill.getProductIdEvent());
            dto.setProductName(bill.getProductName());
            dto.setQty(bill.getQuantity());
            dto.setPrice(bill.getPrice());
            dto.setDiscount(bill.getDiscount());
            return dto;
        }).toList();

        OrderEventDto orderEventDto = new OrderEventDto();
        orderEventDto.setUserName(username);
        orderEventDto.setCustomerEventDto(customerEventDto);
        orderEventDto.setStatus(savedPayment.getPaymentStatus());
        orderEventDto.setPaymentId(savedPayment.getPaymentIdEvent());
        orderEventDto.setId(orderId);
        orderEventDto.setProductItemEventDtos(productItemEventDtos);
        LOGGER.info("📤 Sending event to Kafka: {}", orderEventDto);
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
        return billRepository.findBillByOrderRef(orderIdEvent);
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

    public List<PaymentResponseDto> getAllPaymentsWithProducts() {
        List<PaymentModel> payments = paymentRepository.findAll();
        List<PaymentResponseDto> result = new ArrayList<>();

        for (PaymentModel payment : payments) {
            List<Bill> bills = billRepository.findByOrderRef(payment.getOrderId());
            PaymentResponseDto dto = PaymentMapper.mapToPaymentResponseDto(payment, bills);
            result.add(dto);
        }

        return result;
    }


    public PaymentModel findPaymentById(String paymentIdEvent){
        return paymentRepository.findByPaymentIdEvent(paymentIdEvent)
                .orElseThrow(() -> new RuntimeException("Payment not found for ID: " + paymentIdEvent));
    }
    public  List<Bill> getBills(){
       return billRepository.findAll();
    }

    public Bill findByOrderIdAndProductIdEvent(String orderRef, String productIdEvent) {
        return billRepository.findByOrderRefAndProductIdEvent(orderRef,productIdEvent);
    }

    public PaymentResponseDto findPaymentWithDetails(String orderId) {
        PaymentModel payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found for ID: " + orderId));

        List<Bill> bills = billRepository.findByOrderRef(payment.getOrderId());

        return PaymentMapper.mapToPaymentResponseDto(payment, bills);
    }

    private double calculateDiscount(int qty, double price) {
        double total = qty * price;
        if (total < 100) return 0;
        else if (total < 200) return 0.005 * total;
        else return 0.01 * total;
    }


}
