package com.gogo.payment_service.controller;

import com.gogo.base_domaine_service.dto.Payment;
import com.gogo.payment_service.exception.PaymentNotFoundException;
import com.gogo.payment_service.model.Bill;
import com.gogo.payment_service.model.PaymentModel;
import com.gogo.payment_service.sevice.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/v1")
public class PaymentController {
    @Autowired
    PaymentService paymentService;

    @PostMapping("/payments")
    public String saveAndSendPayment(@RequestBody Payment payment) throws PaymentNotFoundException {
        List<Bill> customerBills=paymentService.findByCustomer(payment.getCustomerIdEvent());
        if(customerBills.isEmpty()){
            throw new PaymentNotFoundException("Customer not available with id: "+payment.getCustomerIdEvent());
        }
        paymentService.saveAndSendPayment(payment);
        return "Payment sent successfully ...";
    }

    @GetMapping("/payments")
    public List<PaymentModel> getAllPayments(){
        return paymentService.findAllPayments();
    }

    @GetMapping("/payments/{paymentIdEvent}")
    public PaymentModel getPayment(@PathVariable String paymentIdEvent){
        return paymentService.findPaymentById(paymentIdEvent);
    }

    @GetMapping("/payments/bills")
    public List<Bill> getAllBills(){
        return paymentService.getBills();
    }

    @GetMapping("/payments/bills/{orderIdEvent}")
    public Bill getBill(@PathVariable String orderIdEvent){
        return paymentService.findByOrderIdEvent(orderIdEvent);
    }

}
