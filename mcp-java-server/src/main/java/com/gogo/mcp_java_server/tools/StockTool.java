package com.gogo.mcp_java_server.tools;

import com.gogo.mcp_java_server.dto.*;
import com.gogo.mcp_java_server.service.StockToolService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StockTool {
    private final StockToolService stockToolService;

    public StockTool(StockToolService stockToolService) {
        this.stockToolService = stockToolService;
    }

    @Tool(description = "Get all products")
    public List<ProductDto> getAllProducts() {
        return stockToolService.getAllProducts();
    }

    @Tool(description = "Get all Customers")
    public List<CustomerDto> getAllCustomers() {
        return stockToolService.getAllCustomers();
    }

    @Tool(description = "Get all orders")
    public List<OrderDto> getAllOrders() {
        return stockToolService.getAllOrders();
    }

    @Tool(description = "Get all order items")
    public List<BillDto> getAllOrderItems() {
        return stockToolService.getAllOrderItems();
    }

    @Tool(description = "Get all delivered orders")
    public List<DeliveredDto> getAllDelivers() {
        return stockToolService.getAllDelivers();
    }

    @Tool(description = "Get all shipped orders")
    public List<ShipDto> getAllShips() {
        return stockToolService.getAllShips();
    }
}
