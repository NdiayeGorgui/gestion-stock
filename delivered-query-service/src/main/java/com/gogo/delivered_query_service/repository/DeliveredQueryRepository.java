package com.gogo.delivered_query_service.repository;


import com.gogo.delivered_query_service.model.Delivered;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeliveredQueryRepository extends JpaRepository<Delivered,Long> {
    Optional<Delivered> findByOrderId(String orderId);


	 Delivered findByOrderIdAndStatus(String orderId, String status);
	 List<Delivered> findByPaymentIdAndOrderIdAndStatus(String paymentId,String orderId, String status);

    boolean existsByOrderIdAndStatus(String orderId, String status);
}
