package com.gogo.inventory_service.controller;


import com.gogo.base_domaine_service.dto.Product;
import com.gogo.inventory_service.exception.ProductNotFoundException;
import com.gogo.inventory_service.kafka.ProductProducer;
import com.gogo.inventory_service.model.ProductModel;
import com.gogo.inventory_service.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class ProductController {

    private final ProductProducer productProducer;
    private final ProductService productService;

    public ProductController(ProductProducer productProducer,ProductService productService) {
        this.productProducer = productProducer;
        this.productService=productService;
    }

    @Operation(
            summary = "save and send Product REST API",
            description = "save and send product REST API to Product object")
    @ApiResponse(responseCode = "200",
            description = "Http status 200 ")

    @PostMapping("/products")
    public ResponseEntity<Map<String, String>> saveAndSendProduct(@RequestBody @Valid Product product){

       productService.saveAndSendProduct(product);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Product sent successfully");
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "get Product REST API",
            description = "get Product REST API from ProductModel object")
    @ApiResponse(responseCode = "200",
            description = "Http status 200 ")

    @GetMapping("/products")
    public List<ProductModel> getProducts(){

        return productService.getAllProducts();

    }

    @Operation(
            summary = "delete Product REST API",
            description = "delete and send Product by productIdEvent REST API from ProductModel object")
    @ApiResponse(responseCode = "200",
            description = "Http status 200 ")


    @DeleteMapping("/products/{productIdEvent}")
    public ResponseEntity<Map<String, String>>  sendCustomer(@PathVariable ("productIdEvent") String productIdEvent) throws ProductNotFoundException {
        ProductModel productModel=productService.findProductById(productIdEvent);
        if(productModel!=null){
            productService.sendProductToDelete(productIdEvent);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Product sent successfully");
            return ResponseEntity.ok(response);
        }
        throw new ProductNotFoundException("Product not available with id: " + productIdEvent);
    }

    @Operation(
            summary = "update product REST API",
            description = "update and send product by productIdEvent REST API from ProductModel object")
    @ApiResponse(responseCode = "200",
            description = "Http status 200 ")

    @PutMapping("/products/{productIdEvent}")
    public ResponseEntity<Map<String, String>> updateAndSendProduct(@RequestBody Product product,@PathVariable("productIdEvent") String productIdEvent) throws ProductNotFoundException {
        ProductModel productModel=productService.findProductById(productIdEvent);
        if(productModel!=null){
            productService.sendProductToUpdate(productIdEvent,product);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Product sent successfully");
            return ResponseEntity.ok(response);
        }
        throw new ProductNotFoundException("Product not available with id: " + productIdEvent);
    }

    @Operation(
            summary = "get Product REST API",
            description = "get Product by productIdEvent REST API from ProductModel object")
    @ApiResponse(responseCode = "200",
            description = "Http status 200 ")

    @GetMapping("/products/{productIdEvent}")
    public ProductModel getProduct(@PathVariable("productIdEvent") String productIdEvent){
        return productService.getProduct(productIdEvent);
    }
}
