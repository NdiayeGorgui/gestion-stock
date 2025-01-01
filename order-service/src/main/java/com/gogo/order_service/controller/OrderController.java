package com.gogo.order_service.controller;

import com.gogo.base_domaine_service.event.OrderEvent;
import com.gogo.base_domaine_service.event.OrderEventDto;
import com.gogo.order_service.kafka.OrderProducer;

import com.gogo.order_service.model.*;
import com.gogo.order_service.repository.ProductRepository;
import com.gogo.order_service.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class OrderController {

    private final OrderProducer orderProducer;
    private final OrderService orderService;
    @Autowired
    private ProductRepository productRepository;

    public OrderController(OrderProducer orderProducer,OrderService orderService) {
        this.orderProducer = orderProducer;
        this.orderService=orderService;
    }

    @PostMapping("/orders")
    public ResponseEntity<?> placeOrder(@RequestBody OrderEvent orderEvent){
       if (orderEvent.getProduct().getQty()>=orderEvent.getProductItem().getProductQty()){
           // save order
           Order savedOrder=new Order();
           orderService.createOrder(orderEvent,savedOrder);
           orderService.saveOrder(savedOrder);

           //save product item
           ProductItem savedProductItem=new ProductItem();
           orderService.createProductItem(orderEvent,savedOrder,savedProductItem);
           orderService.saveProductItem(savedProductItem);

           //send the event to the queue

           OrderEventDto orderEventDto =new OrderEventDto();
           orderService.sendEvent(orderEvent, orderEventDto);
           //recuperer le id du order dans la methode create order pour l'envoyer dans le billing service
           orderEvent.setOrderIdEvent(savedOrder.getOrderIdEvent());
           orderEventDto.setId(orderEvent.getOrderIdEvent());

           orderProducer.sendMessage(orderEventDto);

           return ResponseEntity.ok("Order placed successfully ...");
       }else {
           return ResponseEntity.ok("Insufficient quantity ...");
       }


    }

    @GetMapping("/orders")
    public List<ProductItem> getOrders() {
        orderService.getCustomerAndProduct();
        List<ProductItem> ProductItems=orderService.getOrders();
        return ProductItems;
        }
    @PutMapping("/orders/{orderIdEvent}")
    public String sendOrderToCancel(@RequestBody  OrderEvent orderEvent, @PathVariable("orderIdEvent") String orderIdEvent){

        orderService.sendOrderToCancel(orderEvent,orderIdEvent);

        return "Order for update sent successfully ...";
    }

    @GetMapping("/orders/customer/{customerIdEvent}")
   public List<ProductItem>  findOrdersByCustomer(@PathVariable("customerIdEvent") String customerIdEvent){
        orderService.getCustomerAndProduct();
        List<ProductItem> ProductItems= orderService.getOrderById(customerIdEvent);
        return ProductItems;

    }

    @GetMapping("/orders/{id}")
    public ProductItem  findById(@PathVariable("id") Long id){
        orderService.getCustomerAndProduct();
        ProductItem productItem=orderService.getOrderById(id);
        return productItem;

    }
}
