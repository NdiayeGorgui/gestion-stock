package com.gogo.delivered_command_service.controller;

import com.gogo.base_domaine_service.dto.Payment;
import com.gogo.base_domaine_service.event.EventStatus;
import com.gogo.delivered_command_service.exception.DeliveredCommandNotFoundException;
import com.gogo.delivered_command_service.model.Delivered;
import com.gogo.delivered_command_service.service.DeliveredCommandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

//@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:4300"})
@RestController
@RequestMapping("/api/v1")
public class DeliveredCommandController {

    @Autowired
    private DeliveredCommandService deliveredCommandService;

    @Operation(
            summary = "Send payment REST API",
            description = "Save and Send  Payment REST API to payment object")
    @ApiResponse(responseCode = "200",
            description = "Http status 200")

    @PostMapping("/delivers")
    public ResponseEntity<Map<String, String>> saveAndSendDeliveredCommand(@RequestBody  Delivered delivered) throws DeliveredCommandNotFoundException {

        List<Delivered> deliveredList=deliveredCommandService.findByPaymentAndStatus(delivered.getPaymentId(),EventStatus.DELIVERING.name());
        if(deliveredList.isEmpty()){
            throw new DeliveredCommandNotFoundException("Order not available with PaymentId: "+delivered.getPaymentId());
        }

        deliveredCommandService.saveAndSendDeliveredCommand(delivered);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Deliver sent successfully");
        return ResponseEntity.ok(response);
        }
}
