package com.gogo.billing_service.Repository;

import com.gogo.billing_service.model.Bill;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BillRepository extends JpaRepository<Bill,Long> {
    boolean existsByOrderRefAndStatus(String orderId, String status);
    @Modifying
    @Transactional
    @Query("UPDATE Bill b SET b.status= :status WHERE b.productIdEvent= :productIdEvent")
    int updateBillStatus(@Param("productIdEvent") String productIdEvent, @Param("status") String status);

    Bill  findByOrderRef(String orderRef);

    @Modifying
    @Transactional
    @Query("UPDATE Bill b SET b.status= :status WHERE b.orderRef= :orderRef")
    int updateTheBillStatus(@Param("orderRef") String orderRef, @Param("status") String status);

}
