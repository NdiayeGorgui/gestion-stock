package com.gogo.order_service.repository;

import com.gogo.order_service.model.Order;
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
    void updateOrderStatus(@Param("orderIdEvent") String orderIdEvent, @Param("orderStatus") String orderStatus);

    boolean existsByOrderIdEventAndOrderStatus(String id, String status);

    Order findByOrderIdEvent(String orderRef);

    @Modifying
    @Transactional
    @Query("UPDATE Order o SET o.orderStatus= :orderStatus WHERE o.customerIdEvent= :customerIdEvent")
    void updateAllOrderStatus(@Param("customerIdEvent") String customerIdEvent, @Param("orderStatus") String orderStatus);
    
    
    @Query("SELECT o.customerIdEvent, COUNT(o.id) as orderCount " +
    	       "FROM Order o " +
               "WHERE o.orderStatus = 'COMPLETED' " +
    	       "GROUP BY o.customerIdEvent " +
    	       "ORDER BY orderCount DESC " +
    	       "LIMIT 10")
    	List<Object[]> findTop10CustomersByOrderCount();

    List<Order> findByOrderStatus(String status);

    List<Order> findByCustomerIdEventAndOrderStatus(String customerIdEvent, String status);
}
