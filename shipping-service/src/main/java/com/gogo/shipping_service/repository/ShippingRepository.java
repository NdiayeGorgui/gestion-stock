package com.gogo.shipping_service.repository;

import com.gogo.shipping_service.model.Ship;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShippingRepository extends JpaRepository<Ship,Long> {
    List<Ship> findByPaymentId(String paymentId);

	List<Ship> findByCustomerId(String customerId);

	List<Ship> findByPaymentIdAndOrderIdAndStatus(String paymentId,String orderId, String status);

	Ship findByCustomerIdAndOrderIdAndStatus(String customerId,String orderId, String name);

	Ship findByOrderId(String orderId);
}
