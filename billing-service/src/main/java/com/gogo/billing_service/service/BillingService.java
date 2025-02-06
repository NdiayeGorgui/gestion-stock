package com.gogo.billing_service.service;


import com.gogo.billing_service.Repository.BillRepository;

import com.gogo.billing_service.model.Bill;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BillingService {

    @Autowired
    BillRepository billRepository;
    public Bill getBill(Long id){
        return billRepository.findById(id).orElse(null);
    }
    public void updateBillStatus(String productIdEvent, String status){
        billRepository.updateBillStatus(productIdEvent, status);
    }

    public void updateTheBillStatus(String orderIdEvent, String status){
        billRepository.updateTheBillStatus(orderIdEvent, status);
    }

    public void saveBill(Bill bill){
        billRepository.save(bill);
    }

    public void updateAllBillCustomerStatus(String customerIdEvent,String status){
         billRepository.updateAllBillCustomerStatus(customerIdEvent, status);
    }

    public  Bill  findByOrderRef(String orderRef){
        return billRepository.findByOrderRef(orderRef);
    }

    public List<Bill> billList(String customerIdEvent,String status){
        return billRepository.findByCustomerIdEventAndStatus(customerIdEvent,status);
    }

    public List<Bill> getBills(String customerIdEvent){
        List<Bill> customerBills=billRepository.findAll();
        return customerBills.stream()
                .filter(bill -> bill.getCustomerIdEvent().equalsIgnoreCase(customerIdEvent))
                .collect(Collectors.toList());
    }

    public  double getAmount(String customerIdEvent){
        return billRepository.sumBill(customerIdEvent);
    }
}
