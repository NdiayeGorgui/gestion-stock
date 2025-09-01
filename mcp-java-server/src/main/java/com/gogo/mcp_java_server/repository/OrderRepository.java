package com.gogo.mcp_java_server.repository;

import com.gogo.mcp_java_server.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order,Long> {
    Order findByOrderId(String orderId);
}
