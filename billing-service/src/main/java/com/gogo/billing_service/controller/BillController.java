package com.gogo.billing_service.controller;

import com.gogo.billing_service.model.Bill;
import com.gogo.billing_service.service.BillingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class BillController {
    @Autowired
    BillingService billingService;
    @GetMapping("/bills/{id}")
    public Bill getBill(@PathVariable("id") Long id){
        Bill bill=billingService.getBill(id);
        return bill;

    }
}
