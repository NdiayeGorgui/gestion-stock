package com.gogo.payment_service.repository;

import com.gogo.payment_service.model.Bill;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BillRepository extends JpaRepository <Bill,Long>{
    Bill  findByOrderRef(String orderRef);

    @Modifying
    @Transactional
    @Query("UPDATE Bill b SET b.status= :status WHERE b.orderRef= :orderRef")
    void updateTheBillStatus(@Param("orderRef") String orderRef, @Param("status") String status);

    List<Bill> findByCustomerIdEventAndStatus(String customerIdEvent, String status);

    @Modifying
    @Transactional
    @Query("UPDATE Bill b SET b.status= :status WHERE b.customerIdEvent= :customerIdEvent")
    void updateAllBillCustomerStatus(@Param("customerIdEvent") String customerIdEvent, @Param("status") String status);

    List<Bill> findByCustomerIdEvent(String customerIdEvent);
    List<Bill> findByOrderIdAndStatus(String orderId, String status);

    boolean existsByOrderRefAndStatus(String orderId, String status);
}
