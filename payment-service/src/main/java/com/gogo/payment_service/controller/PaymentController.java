package com.gogo.payment_service.controller;

import com.gogo.base_domaine_service.dto.Payment;
import com.gogo.payment_service.exception.PaymentNotFoundException;
import com.gogo.payment_service.model.Bill;
import com.gogo.payment_service.sevice.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
}
