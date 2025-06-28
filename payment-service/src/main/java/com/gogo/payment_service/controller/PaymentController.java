package com.gogo.payment_service.controller;

import com.gogo.base_domaine_service.dto.Payment;
import com.gogo.payment_service.dto.PaymentResponseDto;
import com.gogo.payment_service.dto.SimplePaymentRequest;
import com.gogo.payment_service.model.Bill;
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



@RestController
@RequestMapping("/api/v1")
public class PaymentController {
    @Autowired
    private PaymentService paymentService;

    @Operation(
            summary = "Save and Send payment REST API",
            description = "Save and Send  Payment REST API to payment object")
    @ApiResponse(responseCode = "200",
            description = "Http status 200")

    @PostMapping("/payments")
    public ResponseEntity<Map<String, String>> processSimplePayment(@RequestBody @Valid SimplePaymentRequest request,
                                                                    @RequestHeader("X-Username") String username) {
        if (request.getOrderId() == null || request.getOrderId().isBlank()) {
            throw new IllegalArgumentException("orderId must not be null or blank");
        }

        Payment payment = new Payment();
        payment.setOrderId(request.getOrderId());
        payment.setPaymentMode(request.getPaymentMode());

        paymentService.saveAndSendPayment(payment,username);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Payment processed for order: " + request.getOrderId());

        return ResponseEntity.ok(response);
    }


    @Operation(
            summary = "get Payments REST API",
            description = "get Payments REST API from PaymentModel object")
    @ApiResponse(responseCode = "200",
            description = "Http status 200 ")

    @GetMapping("/payments")
    public List<PaymentResponseDto> getAllPayments(){
        return paymentService.getAllPaymentsWithProducts();
    }

    @Operation(
            summary = "get Payment REST API",
            description = "get Payment by paymentIdEvent REST API from PaymentModel object")
    @ApiResponse(responseCode = "200",
            description = "Http status 200 ")

    @GetMapping("/payments/{orderRef}")
    public PaymentResponseDto getPayment(@PathVariable("orderRef") String orderRef){
        return paymentService.findPaymentWithDetails(orderRef);
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