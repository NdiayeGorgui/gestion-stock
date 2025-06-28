package com.gogo.delivered_command_service.controller;

import com.gogo.base_domaine_service.event.EventStatus;
import com.gogo.delivered_command_service.exception.DeliveredCommandNotFoundException;
import com.gogo.delivered_command_service.model.Delivered;
import com.gogo.delivered_command_service.service.DeliveredCommandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
/*   Ship existingShip = shippingService.findByOrderIdAndStatus(ship.getOrderId(), EventStatus.SHIPPING.name());

        if (existingShip == null) {
            throw new ShippingNotFoundException("Shipping not found for order: " + ship.getOrderId());
        }

        shippingService.saveAndSendShip(ship);

        Map<String, String> response = new HashMap<>();
        response.put("message", "📦 Shipping sent successfully");
        return ResponseEntity.ok(response);*/
    @PostMapping("/delivers")
    public ResponseEntity<Map<String, String>> saveAndSendDeliveredCommand(@RequestBody Delivered delivered,
                                                                           @RequestHeader("X-Username") String username) throws DeliveredCommandNotFoundException {
        Delivered existingDelivered = deliveredCommandService.findByOrderIdAndStatus(delivered.getOrderId(), EventStatus.DELIVERING.name());

        if (existingDelivered == null ) {
            throw new DeliveredCommandNotFoundException("Delivered not found for order: " + delivered.getOrderId());
        }

        deliveredCommandService.saveAndSendDeliveredCommand(delivered,username);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Deliver sent successfully");
        return ResponseEntity.ok(response);
    }
}
