package com.gogo.mcp_java_server.service;

import com.gogo.mcp_java_server.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class StockToolService {
    @Autowired
    private StockService stockService;


    public List<CustomerDto> getAllCustomers() {
        return stockService.getAllCustomers().stream()
                .map(c -> new CustomerDto(c.getCustomerId(), c.getName(), c.getAddress(), c.getCity(), c.getPostalCode(), c.getPhone(), c.getEmail(), c.getStatus(),c.getCreatedDate()))
                .toList();
    }

    public List<ProductDto> getAllProducts() {
        return stockService.getAllProducts().stream()
                .map(p -> new ProductDto(p.getProductId(), p.getName(), p.getCategory(), p.getDescription(), p.getLocation(), p.getQty(), p.getPrice(), p.getStatus(), p.getQtyStatus(), p.getCreatedDate()))
                .toList();
    }

    public List<OrderDto> getAllOrders() {
        return stockService.getAllOrders().stream()
                .map(o -> new OrderDto(o.getOrderId(), o.getDate(),o.getCustomerId(), o.getOrderStatus()))
                .toList();
    }


    public List<BillDto> getAllOrderItems() {
        return stockService.getAllOrderItems().stream()
                .map(b -> new BillDto(b.getProductId(), b.getCustomerId(), b.getCustomerName(), b.getCustomerPhone(), b.getCustomerMail(),b.getOrderRef(),b.getQuantity(),b.getPrice(), b.getDiscount(), b.getStatus(),b.getBillingDate()))
                .toList();
    }

    public List<DeliveredDto> getAllDelivers() {
        return stockService.getAllDelivers().stream()
                .map(d -> new DeliveredDto(d.getOrderId(),  d.getCustomerId(), d.getCustomerName(), d.getCustomerMail(), d.getStatus(), d.getDetails(),d.getEventTimeStamp()))
                .toList();
    }

    public List<ShipDto> getAllShips() {
        return stockService.getAllShips().stream()
                .map(s -> new ShipDto(s.getOrderId(), s.getCustomerId(), s.getCustomerName(), s.getCustomerMail(), s.getStatus(), s.getDetails(),s.getEventTimeStamp()))
                .toList();
    }
}
