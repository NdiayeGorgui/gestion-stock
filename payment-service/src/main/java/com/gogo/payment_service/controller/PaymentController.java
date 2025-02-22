package com.gogo.payment_service.controller;

import com.gogo.base_domaine_service.dto.Payment;
import com.gogo.payment_service.exception.PaymentNotFoundException;
import com.gogo.payment_service.model.Bill;
import com.gogo.payment_service.model.PaymentModel;
import com.gogo.payment_service.sevice.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/v1")
public class PaymentController {
    @Autowired
    PaymentService paymentService;

    @Operation(
            summary = "Save and Send payment REST API",
            description = "Save and Send  Payment REST API to payment object")
    @ApiResponse(responseCode = "200",
            description = "Http status 200")

    @PostMapping("/payments")
    public  ResponseEntity<Map<String, String>> saveAndSendPayment(@RequestBody @Valid Payment payment) throws PaymentNotFoundException {
        List<Bill> customerBills=paymentService.findByCustomer(payment.getCustomerIdEvent());
        if(customerBills.isEmpty()){
            throw new PaymentNotFoundException("Customer not available with id: "+payment.getCustomerIdEvent());
        }
        paymentService.saveAndSendPayment(payment);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Payment sent successfully");
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "get Payments REST API",
            description = "get Payments REST API from PaymentModel object")
    @ApiResponse(responseCode = "200",
            description = "Http status 200 ")

    @GetMapping("/payments")
    public List<PaymentModel> getAllPayments(){
        return paymentService.findAllPayments();
    }

    @Operation(
            summary = "get Payment REST API",
            description = "get Payment by paymentIdEvent REST API from PaymentModel object")
    @ApiResponse(responseCode = "200",
            description = "Http status 200 ")

    @GetMapping("/payments/{paymentIdEvent}")
    public PaymentModel getPayment(@PathVariable("paymentIdEvent") String paymentIdEvent){
        return paymentService.findPaymentById(paymentIdEvent);
    }

    @Operation(
            summary = "get Bills REST API",
            description = "get Bills  REST API from Bill object")
    @ApiResponse(responseCode = "200",
            description = "Http status 200 ")

    @GetMapping("/payments/bills")
    public List<Bill> getAllBills(){
        return paymentService.getBills();
    }

    @Operation(
            summary = "get Bill REST API",
            description = "get Bill by orderIdEvent REST API from Bill object")
    @ApiResponse(responseCode = "200",
            description = "Http status 200 ")

    @GetMapping("/payments/bills/{orderIdEvent}")
    public Bill getBill(@PathVariable ("orderIdEvent") String orderIdEvent){
        return paymentService.findByOrderIdEvent(orderIdEvent);
    }

}
//http://localhost:8085/swagger-ui/index.html