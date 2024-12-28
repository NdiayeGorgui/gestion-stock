package com.gogo.billing_service.service;


import com.gogo.billing_service.Repository.BillRepository;

import com.gogo.billing_service.model.Bill;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BillingService {

    @Autowired
    BillRepository billRepository;
    public Bill getBill(Long id){
        Bill bill=billRepository.findById(id).orElse(null);
        return bill;
    }
    public int updateBillStatus(String productIdEvent,String status){
        return billRepository.updateBillStatus(productIdEvent,status);
    }

    public int updateTheBillStatus(String orderIdEvent,String status){
        return billRepository.updateTheBillStatus(orderIdEvent,status);
    }

    public  Bill  findByOrderRef(String orderRef){
        return billRepository.findByOrderRef(orderRef);
    }
}
