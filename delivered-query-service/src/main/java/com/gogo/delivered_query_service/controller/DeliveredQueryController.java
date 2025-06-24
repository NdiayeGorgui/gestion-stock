package com.gogo.delivered_query_service.controller;


import com.gogo.delivered_query_service.dto.DeliveredResponseDto;
import com.gogo.delivered_query_service.service.DeliveredQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


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
    public List<DeliveredResponseDto> getAllShips(){
        return deliveredQueryService.getAllShipsWithProducts();
    }


    /*@Operation(
            summary = "get Ship REST API",
            description = "get Delivered by orderId REST API ")
    @ApiResponse(responseCode = "200",
            description = "Http status 200 ")
    @GetMapping("/delivers/{orderId}")
    public DeliveredResponseDto getDeliveredByOrder(@PathVariable("orderId") String orderId) {
        Delivered delivered = deliveredQueryService.findByOrderId(orderId);
        return DeliveredMapper.toDto(delivered);
    }*/

    @Operation(
            summary = "get Delivered REST API",
            description = "get Delivered by orderId REST API from Delivered object")
    @ApiResponse(responseCode = "200",
            description = "Http status 200 ")
    @GetMapping("/delivers/{orderId}")
    public DeliveredResponseDto getDeliver(@PathVariable("orderId") String orderId){
        return deliveredQueryService.findDeliverWithDetails(orderId);
    }
}
