package com.gogo.shipping_service.controller;

import com.gogo.base_domaine_service.event.EventStatus;
import com.gogo.shipping_service.dto.ShipResponseDto;
import com.gogo.shipping_service.exception.ShippingNotFoundException;
import com.gogo.shipping_service.model.Ship;
import com.gogo.shipping_service.service.ShippingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/v1")
public class ShippingController {

    @Autowired
    private ShippingService shippingService;


    @Operation(
            summary = "Save and Send Shipment REST API",
            description = "Save and Send  Ship REST API to payment object")
    @ApiResponse(responseCode = "200",
            description = "Http status 200")

    @PostMapping("/ships")
    public ResponseEntity<Map<String, String>> saveAndSendShip(@RequestBody Ship ship,@RequestHeader("X-Username") String username) throws ShippingNotFoundException {
        Ship existingShip = shippingService.findByOrderIdAndStatus(ship.getOrderId(), EventStatus.SHIPPING.name());

        if (existingShip == null) {
            throw new ShippingNotFoundException("Shipping not found for order: " + ship.getOrderId());
        }

        shippingService.saveAndSendShip(ship,username);

        Map<String, String> response = new HashMap<>();
        response.put("message", "📦 Shipping sent successfully");
        return ResponseEntity.ok(response);
    }




    @Operation(
            summary = "get ships REST API",
            description = "get Ships REST API from Ship object")
    @ApiResponse(responseCode = "200",
            description = "Http status 200 ")
    @GetMapping("/ships")
    public List<ShipResponseDto> getAllShips(){
        return shippingService.getAllShipsWithProducts();
    }


    @Operation(
            summary = "get Ship REST API",
            description = "get Ship by orderId REST API from Ship object")
    @ApiResponse(responseCode = "200",
            description = "Http status 200 ")
    @GetMapping("/ships/{orderId}")
    public ShipResponseDto getShip(@PathVariable("orderId") String orderId){
        return shippingService.findShipWithDetails(orderId);
    }
  /*  public Ship getShipsByOrder(@PathVariable("orderId") String orderId){
        return shippingService.findByOrderId(orderId);
    }*/

}
