package com.gogo.shipping_service.controller;

import com.gogo.shipping_service.model.Ship;
import com.gogo.shipping_service.service.ShippingService;
import com.gogo.shipping_service.exception.ShippingNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

//@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:4300"})
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
    public ResponseEntity<Map<String, String>> saveAndSendShip(@RequestBody  Ship ship) throws ShippingNotFoundException {
        List<Ship> customerShips=shippingService.findByCustomer(ship.getCustomerId());
         if(customerShips.isEmpty()){
            throw new ShippingNotFoundException("Customer not available with id: "+ship.getCustomerId());
        }
        shippingService.saveAndSendShip(ship);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Ship sent successfully");
        return ResponseEntity.ok(response);
    }


    @Operation(
            summary = "get ships REST API",
            description = "get Ships REST API from Ship object")
    @ApiResponse(responseCode = "200",
            description = "Http status 200 ")
    @GetMapping("/ships")
    public List<Ship> getAllShips(){
        return shippingService.findAllShips();
    }

   
    
    @Operation(
            summary = "get Ship REST API",
            description = "get Ship by orderId REST API from Ship object")
    @ApiResponse(responseCode = "200",
            description = "Http status 200 ")
    @GetMapping("/ships/{orderId}")
    public Ship getShipsByOrder(@PathVariable("orderId") String orderId){
        return shippingService.findByOrderId(orderId);
    }

}
