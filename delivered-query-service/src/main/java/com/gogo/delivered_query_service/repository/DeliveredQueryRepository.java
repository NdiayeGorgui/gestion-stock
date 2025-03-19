package com.gogo.delivered_query_service.repository;


import com.gogo.delivered_query_service.model.Delivered;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveredQueryRepository extends JpaRepository<Delivered,Long> {
    Delivered findByOrderId(String orderId);


	 Delivered findByCustomerIdAndOrderIdAndStatus(String customerId,String orderId, String status);
	 List<Delivered> findByPaymentIdAndOrderIdAndStatus(String paymentId,String orderId, String status);
}
