package com.gogo.order_service.repository;


import com.gogo.order_service.model.OrderEventSourcing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderEventRepository extends JpaRepository<OrderEventSourcing,Long> {
    List<OrderEventSourcing> findByOrderIdAndStatus(String id,String status);
    List<OrderEventSourcing> findByStatusAndCustomerId(String status,String id);

    List<OrderEventSourcing> findByOrderIdAndStatusIn(String orderId, List<String> list);
}
