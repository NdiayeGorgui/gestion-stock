package com.gogo.delivered_query_service.controller;


import com.gogo.delivered_query_service.model.Delivered;
import com.gogo.delivered_query_service.service.DeliveredQueryService;

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
public class DeliveredQueryController {

    @Autowired
    private DeliveredQueryService deliveredQueryService;

    @Operation(
            summary = "Save and Send delivered REST API",
            description = "Save and Send  delivered REST API")
    @ApiResponse(responseCode = "200",
            description = "Http status 200")

    @GetMapping("/delivers")
    public List<Delivered> getAllDelivers()  {
       return  deliveredQueryService.getAllDelivers();
        }
    
    @Operation(
            summary = "get Ship REST API",
            description = "get Delivered by orderId REST API ")
    @ApiResponse(responseCode = "200",
            description = "Http status 200 ")
    @GetMapping("/delivers/{orderId}")
    public Delivered getDeliveredByOrder(@PathVariable("orderId") String orderId){
        return deliveredQueryService.findByOrderId(orderId);
    }
}
