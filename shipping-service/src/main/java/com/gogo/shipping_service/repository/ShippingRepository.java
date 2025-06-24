package com.gogo.shipping_service.repository;

import com.gogo.shipping_service.model.Ship;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShippingRepository extends JpaRepository<Ship,Long> {
	List<Ship> findByPaymentId(String paymentId);

	List<Ship> findByCustomerId(String customerId);

	List<Ship> findByPaymentIdAndOrderIdAndStatus(String paymentId,String orderId, String status);

	Ship findByCustomerIdAndOrderIdAndStatus(String customerId,String orderId, String status);

	//Ship findByOrderId(String orderId);

	Ship findByOrderIdAndStatus(String orderId, String status);

	Optional<Ship> findByOrderId(String orderId);

    boolean existsByOrderIdAndStatus(String orderId, String name);
}
