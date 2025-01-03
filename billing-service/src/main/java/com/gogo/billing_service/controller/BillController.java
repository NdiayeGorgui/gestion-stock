package com.gogo.billing_service.controller;

import com.gogo.billing_service.exception.BillNotFoundException;
import com.gogo.billing_service.model.Bill;
import com.gogo.billing_service.service.BillExcelExporter;
import com.gogo.billing_service.service.BillingService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1")
public class BillController {
    @Autowired
    BillingService billingService;

    @GetMapping("/bills/{id}")
    public Bill getBill(@PathVariable("id") Long id) throws BillNotFoundException {
        Optional<Bill> bill = Optional.ofNullable(billingService.getBill(id));
        if (bill.isPresent()) {
            return bill.get();
        }
            throw new BillNotFoundException("Bill not available with id: " + id );
    }

    @GetMapping("/bills/{customerIdEvent}/{status}")
    public List<Bill> getCustomerBillsStatus(@PathVariable("customerIdEvent") String customerIdEvent,@PathVariable("status") String status) throws BillNotFoundException {
        List<Bill> bills=billingService.getBills(customerIdEvent);
        if(bills.isEmpty()){
            throw new BillNotFoundException("Customer not available with id: "+customerIdEvent);
        }
        return billingService.billList(customerIdEvent,status);
    }

    @GetMapping("/bills/export/{customerIdEvent}/{status}")
    public void exportToExcel(HttpServletResponse response,@PathVariable("customerIdEvent") String customerIdEvent,@PathVariable("status") String status) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date(0));

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=bills" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);

        List<Bill> billList = billingService.billList(customerIdEvent,status);
        if(billList!=null){
            BillExcelExporter excelExporter = new BillExcelExporter(billList);

            excelExporter.export(response,customerIdEvent,status);
        }else {
            throw new RuntimeException("Empty list");
        }
    }
}
