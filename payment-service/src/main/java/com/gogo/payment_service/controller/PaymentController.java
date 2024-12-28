package com.gogo.payment_service.controller;

import com.gogo.base_domaine_service.dto.Customer;
import com.gogo.base_domaine_service.dto.Payment;
import com.gogo.base_domaine_service.event.PaymentEvent;
import com.gogo.payment_service.sevice.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class PaymentController {
    @Autowired
    PaymentService paymentService;

    @PostMapping("/payments")
    public String saveAndSendPayment(@RequestBody Payment payment) {
        paymentService.saveAndSendPayment(payment);

        return "Payment sent successfully ...";
    }
}
