package com.gogo.delivered_command_service.repository;

import com.gogo.delivered_command_service.model.Delivered;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeliveredCommandRepository extends JpaRepository<Delivered,Long> {
    List<Delivered> findByPaymentId(String paymentId);
    List<Delivered> findByPaymentIdAndStatus(String paymentId, String status);
    Delivered findByCustomerIdAndOrderIdAndStatus(String customerId,String orderId, String status);
    List<Delivered> findByPaymentIdAndOrderIdAndStatus(String paymentId,String orderId, String status);
}
