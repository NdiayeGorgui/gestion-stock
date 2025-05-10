package com.gogo.billing_service.controller;

import com.gogo.billing_service.exception.BillNotFoundException;
import com.gogo.billing_service.model.Bill;
import com.gogo.billing_service.service.BillExcelExporter;
import com.gogo.billing_service.service.BillingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:4300"})
@RestController
@RequestMapping("/api/v1")
public class BillController {
    @Autowired
    private BillingService billingService;

    @Operation(
            summary = "get Bill REST API",
            description = "get Bill by id REST API from Bill object")
    @ApiResponse(responseCode = "200",
            description = "Http status 200 ")

    @GetMapping("/bills/{id}")
    public Bill getBill(@PathVariable("id") Long id) throws BillNotFoundException {
        Optional<Bill> bill = Optional.ofNullable(billingService.getBill(id));
        if (bill.isPresent()) {
            return bill.get();
        }
            throw new BillNotFoundException("Bill not available with id: " + id );
    }

    @Operation(
            summary = "get Bills REST API",
            description = "get Bills by customerIdEvent and status REST API from Bill object")
    @ApiResponse(responseCode = "200",
            description = "Http status 200 ")

    @GetMapping("/bills/{customerIdEvent}/{status}")
    public List<Bill> getCustomerBillsStatus(@PathVariable("customerIdEvent") String customerIdEvent,@PathVariable("status") String status) throws BillNotFoundException {
        List<Bill> bills=billingService.getBills(customerIdEvent);
        if(bills.isEmpty()){
            throw new BillNotFoundException("Customer not available with id: "+customerIdEvent);
        }
        return billingService.billList(customerIdEvent,status);
    }
    @Operation(
            summary = "get and print Bill REST API",
            description = "get and print excel file Bill by customerIdEvent and status REST API from Bill object")
    @ApiResponse(responseCode = "200",
            description = "Http status 200 ")

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

    @Operation(
            summary = "get Bills REST API",
            description = "get Bills REST API from Bill object")
    @ApiResponse(responseCode = "200",
            description = "Http status 200 ")

    @GetMapping("/bills")
    public List<Bill> getAllBills(){
        return billingService.getBills();
    }

    @Operation(
            summary = "get Bills REST API",
            description = "get Bills by orderIdEvent REST API from Bill object")
    @ApiResponse(responseCode = "200",
            description = "Http status 200 ")

    @GetMapping("/bills/bill/{orderIdEvent}")
    public Bill getBill(@PathVariable ("orderIdEvent") String orderIdEvent){
        return billingService.findByOrderIdEvent(orderIdEvent);
    }
}
