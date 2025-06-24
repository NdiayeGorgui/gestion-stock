package com.gogo.payment_service.repository;

import com.gogo.payment_service.model.PaymentModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<PaymentModel,Long> {

    Optional<PaymentModel> findByPaymentIdEvent(String paymentIdEvent);

    Optional<PaymentModel> findByOrderId(String orderId);

    boolean existsByOrderId(String orderId);
}
