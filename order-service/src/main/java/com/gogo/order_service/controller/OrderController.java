package com.gogo.order_service.controller;

import com.gogo.base_domaine_service.event.OrderEvent;
import com.gogo.base_domaine_service.event.OrderEventDto;
import com.gogo.order_service.dto.AmountDto;
import com.gogo.order_service.dto.CustomerDto;
import com.gogo.order_service.dto.ProductStatDTO;
import com.gogo.order_service.kafka.OrderProducer;

import com.gogo.order_service.model.*;
import com.gogo.order_service.repository.ProductRepository;
import com.gogo.order_service.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:4300"})
@RestController
@RequestMapping("/api/v1")
public class OrderController {

    private final OrderProducer orderProducer;
    private final OrderService orderService;
    @Autowired
    private ProductRepository productRepository;
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    public OrderController(OrderProducer orderProducer,OrderService orderService) {
        this.orderProducer = orderProducer;
        this.orderService=orderService;
    }

    @Operation(
            summary = "Send order REST API",
            description = "Send and Save Order REST API to save order object")
    @ApiResponse(responseCode = "200",
            description = "Http status 200")

    @PostMapping("/orders")
    public ResponseEntity<Map<String, String>> placeOrder(@RequestBody OrderEvent orderEvent) {
        // Vérifier si le produit existe
        Product product = orderService.findProductById(orderEvent.getProduct().getProductIdEvent());

        if (product == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Collections.singletonMap("error", "Product must not be null"));
        }

        // Vérifier la quantité en stock
        if (product.getQty() < orderEvent.getProductItem().getProductQty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Collections.singletonMap("error", "Insufficient quantity"));
        }

        // Vérifier la quantité saisie
        if (orderEvent.getProductItem().getProductQty() < 1) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Collections.singletonMap("error", "Quantity must be not under 1"));
        }

        // Créer et enregistrer la commande
        Order savedOrder = new Order();
        orderService.createOrder(orderEvent, savedOrder);
        orderService.saveOrder(savedOrder);

        // Créer et enregistrer l'élément du produit
        ProductItem savedProductItem = new ProductItem();
        orderService.createProductItem(orderEvent, savedOrder, savedProductItem);
        orderService.saveProductItem(savedProductItem);

        // Préparer et envoyer l'événement à la file d'attente
        OrderEventDto orderEventDto = new OrderEventDto();
        orderService.sendEvent(orderEvent, orderEventDto);

        // Récupérer et envoyer l'ID de la commande
        orderEvent.setOrderIdEvent(savedOrder.getOrderIdEvent());
        orderEventDto.setPaymentId(savedOrder.getOrderId());
        orderEventDto.setId(orderEvent.getOrderIdEvent());

        logger.info("Order created successfully with ID: {}", savedOrder.getOrderIdEvent());

        orderProducer.sendMessage(orderEventDto);

        // Retourner la réponse
        Map<String, String> response = new HashMap<>();
        response.put("message", "Order sent successfully!");
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "get Order REST API",
            description = "get Order REST API from ProductItem object")
    @ApiResponse(responseCode = "200",
            description = "Http status 200 ")

    @GetMapping("/orders")
    public List<ProductItem> getOrders() {
        orderService.getCustomerAndProduct();
        return orderService.getOrders();
        }
    
    @Operation(
            summary = "get Order REST API",
            description = "get Order Events REST API ")
    @ApiResponse(responseCode = "200",
            description = "Http status 200 ")

    @GetMapping("/orders/events/all")
    public List<OrderEventSourcing> getOrderEvents() {
       
        return orderService.getOrderEvents();
        }

    @Operation(
            summary = "get Orders by status REST API",
            description = "get Order by status(CREATED, COMPLETED, CANCELLED) REST API from ProductItem list object")
    @ApiResponse(responseCode = "200",
            description = "Http status 200 ")

    @GetMapping("/orders/status/{status}")
    public List<ProductItem> getCreatedOrders(@PathVariable("status") String status) {
        orderService.getCustomerAndProduct();
        return orderService.getCreatedOrders(status);
    }
    @Operation(
            summary = "get Order REST API",
            description = "send orderIdEvent for updating  order from orderIdEvent object")
    @ApiResponse(responseCode = "200",
            description = "Http status 200 ")

    @GetMapping("/orders/update/{orderIdEvent}")
    public  ResponseEntity<Map<String, String>> sendOrderToCancel( @PathVariable("orderIdEvent") String orderIdEvent){

        orderService.sendOrderToCancel(orderIdEvent);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Order for cancel sent successfully");
        return ResponseEntity.ok(response);
    }
    @Operation(
            summary = "get Order REST API",
            description = "send orderIdEvent for order confirmation")
    @ApiResponse(responseCode = "200",
            description = "Http status 200 ")
    
    @GetMapping("/orders/confirm/{orderIdEvent}")
    public  ResponseEntity<Map<String, String>> sendOrderToConfirm( @PathVariable("orderIdEvent") String orderIdEvent){

        orderService.sendOrderToConfirm(orderIdEvent);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Order for confirmation sent successfully");
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "get Orders by customerIdEvent REST API",
            description = "get orders by customerIdEvent  REST API from ProductItem list object")
    @ApiResponse(responseCode = "200",
            description = "Http status 200 ")

    @GetMapping("/orders/customer/{customerIdEvent}")
   public List<ProductItem>  findOrdersByCustomer(@PathVariable("customerIdEvent") String customerIdEvent){
        orderService.getCustomerAndProduct();
        return orderService.getOrderById(customerIdEvent);
    }

    @Operation(
            summary = "get Amount by customerIdEvent and status REST API",
            description = "get amount by customerIdEvent and status  REST API from AmountDto object")
    @ApiResponse(responseCode = "200",
            description = "Http status 200 ")

    @GetMapping("/orders/customers/{customerIdEvent}/{status}")
    public AmountDto getAmount(@PathVariable("customerIdEvent") String customerIdEvent, @PathVariable("status") String status){
        return orderService.getAmount(customerIdEvent,status);
    }


    @GetMapping("/orders/customer/{customerIdEvent}/{status}")
    public List<ProductItem>  findOrdersByCustomerId(@PathVariable("customerIdEvent") String customerIdEvent,@PathVariable("status") String status){
        orderService.getCustomerAndProduct();
        return orderService.findByOrderCustomerIdEventAndStatus(customerIdEvent,status);
    }
    @Operation(
            summary = "get Order by orderIdEvent REST API",
            description = "get order by orderIdEvent  REST API from ProductItem object")
    @ApiResponse(responseCode = "200",
            description = "Http status 200 ")

    @GetMapping("/orders/order/{orderIdEvent}")
    public ProductItem  findOrdersByOrderIdEvent(@PathVariable("orderIdEvent") String orderIdEvent){
        orderService.getCustomerAndProduct();
        return orderService.findProductItemByOrderEventId(orderIdEvent);
    }

    @Operation(
            summary = "get Order by id REST API",
            description = "get order by id  REST API from ProductItem object")
    @ApiResponse(responseCode = "200",
            description = "Http status 200 ")

    @GetMapping("/orders/byId/{id}")
    public ProductItem  findOrderById(@PathVariable("id") Long id){
        orderService.getCustomerAndProduct();
        return orderService.getOrderById(id);
    }


    @Operation(
            summary = "get products  REST API",
            description = "get products REST API from Product object")
    @ApiResponse(responseCode = "200",
            description = "Http status 200 ")

    @GetMapping("/orders/products")
    public List<Product>   findAllProducts(){
        return  orderService.getProducts();
    }


    @Operation(
            summary = "get customers  REST API",
            description = "get customers REST API from Customer object")
    @ApiResponse(responseCode = "200",
            description = "Http status 200 ")

    @GetMapping("/orders/customers")
    public List<Customer>   findAllCustomers(){
        return  orderService.getCustomers();
    }

    @Operation(
            summary = "get product  REST API",
            description = "get product by productIdEvent REST API from Product object")
    @ApiResponse(responseCode = "200",
            description = "Http status 200 ")

    @GetMapping("/orders/products/{productIdEvent}")
    public Product   findProductById(@PathVariable ("productIdEvent") String productIdEvent){
        return  orderService.findProductById(productIdEvent);
    }

    @Operation(
            summary = "get customer  REST API",
            description = "get customer by customerIdEvent REST API from Customer object")
    @ApiResponse(responseCode = "200",
            description = "Http status 200 ")

    @GetMapping("/orders/customers/{customerIdEvent}")
    public Customer   findCustomerById(@PathVariable ("customerIdEvent") String customerIdEvent){
        return  orderService.findCustomerById(customerIdEvent);
    }
    
    @Operation(
            summary = "get customer  REST API",
            description = "get most ordered product REST API ")
    @ApiResponse(responseCode = "200",
            description = "Http status 200 ")
    
    @GetMapping("/orders/most-ordered-products")
    public List<ProductStatDTO> getProduitsLesPlusCommandes() {
        return orderService.getMostOrderedProducts();
    }
    
    @Operation(
            summary = "get customer  REST API",
            description = "get top 10 customers REST API ")
    @ApiResponse(responseCode = "200",
            description = "Http status 200 ")
    
    @GetMapping("/orders/top10")
    public ResponseEntity<List<CustomerDto>> getTopCustomers() {
        return ResponseEntity.ok(orderService.getTop10Customers());
    }
}
//http://localhost:8081/swagger-ui/index.html