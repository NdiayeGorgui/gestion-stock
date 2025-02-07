package com.gogo.payment_service.repository;

import com.gogo.payment_service.model.PaymentModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<PaymentModel,Long> {

    PaymentModel findByPaymentIdEvent(String paymentIdEvent);
}
