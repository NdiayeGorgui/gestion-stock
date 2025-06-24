package com.gogo.delivered_command_service.controller;

import com.gogo.delivered_command_service.exception.DeliveredCommandNotFoundException;
import com.gogo.delivered_command_service.service.DeliveredCommandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;


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
    public ResponseEntity<Map<String, String>> saveAndSendDeliveredCommand(@RequestBody Map<String, String> req) throws DeliveredCommandNotFoundException {
        String orderId = req.get("orderId");

        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("orderId must not be null or empty");
        }

        deliveredCommandService.saveAndSendDeliveredCommand(orderId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Deliver sent successfully for order: " + orderId);
        return ResponseEntity.ok(response);
    }
}
