package com.gogo.order_service.repository;

import com.gogo.order_service.model.Order;
import com.gogo.order_service.model.ProductItem;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order,Long> {

    List<Order> findByCustomerIdEvent(String id);
    @Modifying
    @Transactional
    @Query("UPDATE Order o SET o.orderStatus= :orderStatus WHERE o.orderIdEvent= :orderIdEvent")
    int updateOrderStatus(@Param("orderIdEvent") String orderIdEvent, @Param("orderStatus") String orderStatus);

    boolean existsByOrderIdEventAndOrderStatus(String id, String status);

    Order findByOrderIdEvent(String orderRef);


}