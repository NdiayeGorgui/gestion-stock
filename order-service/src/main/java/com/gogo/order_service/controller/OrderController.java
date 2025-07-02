package com.gogo.order_service.controller;

import com.gogo.base_domaine_service.event.OrderEventDto;
import com.gogo.order_service.dto.*;
import com.gogo.order_service.kafka.OrderProducer;
import com.gogo.order_service.model.*;
import com.gogo.order_service.repository.ProductRepository;
import com.gogo.order_service.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:4300"})
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
    public ResponseEntity<?> placeOrder(@RequestBody CommandEvent commandEvent,
                                        @RequestHeader("X-Username") String username) {

        List<String> errors = new ArrayList<>();
        List<ProductItemRequest> requests = commandEvent.getProductItems();

        // Étape 1 : VALIDATION de toutes les lignes de commande
        for (ProductItemRequest request : requests) {
            Product product = orderService.findProductById(request.getProductIdEvent());

            if (product == null) {
                errors.add("❌ Product not found : " + request.getProductIdEvent());
                continue;
            }

            if (request.getProductQty() < 1) {
                errors.add("❌ Invalid quantity for the product : " + product.getName());
            }

            if (product.getQty() < request.getProductQty()) {
                errors.add("❌ Insufficient stock for product : " + product.getName() +
                        " (current stock : " + product.getQty() + ", requested : " + request.getProductQty() + ")");
            }
        }

        // Si une erreur est détectée, retour immédiat avec la liste des erreurs
        if (!errors.isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("errors", errors);
            return ResponseEntity.badRequest().body(response);
        }

        // Étape 2 : Création de la commande
        Order order = orderService.createOrder(commandEvent);

        // Étape 3 : Création des ProductItems
        List<ProductItem> savedItems = new ArrayList<>();
        for (ProductItemRequest request : requests) {
            Product product = orderService.findProductById(request.getProductIdEvent());
            ProductItem item = new ProductItem();
            orderService.createProductItemFromRequest(request, product, order, item);
            orderService.saveProductItem(item);
            savedItems.add(item);
        }

        // Étape 4 : Construction de l’événement
        OrderEventDto eventDto = orderService.buildEventDto(order, savedItems, username);

        // Étape 5 : Envoi Kafka
        orderProducer.sendMessage(eventDto);

        // Étape 6 : Réponse
        Map<String, String> response = new HashMap<>();
        response.put("message", "✅ Order created successfully !");
        response.put("orderIdEvent", order.getOrderIdEvent());

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "get Order REST API",
            description = "get Order REST API from ProductItem object")
    @ApiResponse(responseCode = "200",
            description = "Http status 200 ")
    @GetMapping("/orders")
    public List<OrderResponseDto> getOrders() {
        return orderService.getAllOrdersWithDetails();
    }

   /* @GetMapping("/orders")
    public List<ProductItem> getOrders() {
        orderService.getCustomerAndProduct();
        return orderService.getOrders();
    }*/

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<OrderResponseDto> getOrderById(@PathVariable("orderId") String orderId) {
        OrderResponseDto dto = orderService.getOrderWithDetailsById(orderId);
        return ResponseEntity.ok(dto);
    }

   @GetMapping("/orders/status/{status}")
    public List<OrderResponseDto> getOrdersByStatus(@PathVariable("status") String status) {
        orderService.getCustomerAndProduct(); // <- Optionnel si tu veux aussi enrichir les clients
        return orderService.getOrdersWithDetailsByStatus(status);
    }

    @GetMapping("/orders/status/{status}/id/{orderId}")
    public ResponseEntity<OrderResponseDto> getOrderByStatusAndId(@PathVariable("status") String status, @PathVariable("orderId") String orderId) {
        OrderResponseDto dto = orderService.getOrderWithDetailsByStatusAndId(status, orderId);
        if (dto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(dto);
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

    @GetMapping("/orders/details/status/{status}")
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
    public  ResponseEntity<Map<String, String>> sendOrderToCancel( @PathVariable("orderIdEvent") String orderIdEvent, @RequestHeader("X-Username") String username){

        orderService.sendOrderToCancel(orderIdEvent,username);

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
    public List<OrderResponseDto>  findOrdersByCustomer(@PathVariable("customerIdEvent") String customerIdEvent) {

        return orderService.getOrdersWithDetailsByCustomer(customerIdEvent);
    }

   /* @GetMapping("/orders/customer/{customerIdEvent}")
    public List<ProductItem>  findOrdersByCustomer(@PathVariable("customerIdEvent") String customerIdEvent){
        orderService.getCustomerAndProduct();
        return orderService.getOrderById(customerIdEvent);
    }*/

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
    public List<OrderResponseDto> findOrdersByCustomerAndStatus(
            @PathVariable("customerIdEvent") String customerIdEvent,
            @PathVariable("status") String status) {

        return orderService.getOrdersWithDetailsByCustomerAndStatus(customerIdEvent, status.toUpperCase());
    }


    /*@GetMapping("/orders/customer/{customerIdEvent}/{status}")
    public List<ProductItem>  findOrdersByCustomerId(@PathVariable("customerIdEvent") String customerIdEvent,@PathVariable("status") String status){
        orderService.getCustomerAndProduct();
        return orderService.findByOrderCustomerIdEventAndStatus(customerIdEvent,status);
    }*/
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