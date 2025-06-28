package com.gogo.billing_service.Repository;

import com.gogo.billing_service.model.Bill;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BillRepository extends JpaRepository<Bill,Long> {
    boolean existsByOrderRefAndStatus(String orderId, String status);
    @Modifying
    @Transactional
    @Query("UPDATE Bill b SET b.status= :status WHERE b.productIdEvent= :productIdEvent")
    void updateBillStatus(@Param("productIdEvent") String productIdEvent, @Param("status") String status);

    //Bill  findByOrderRef(String orderRef);
    List<Bill> findAllByOrderRef(String orderRef);


    @Modifying
    @Transactional
    @Query("UPDATE Bill b SET b.status= :status WHERE b.orderRef= :orderRef")
    void updateTheBillStatus(@Param("orderRef") String orderRef, @Param("status") String status);

    @Query("SELECT sum(b.price) from Bill b where b.customerIdEvent = ?1")
    double sumBill( String customerIdEvent);

    List<Bill> findByCustomerIdEventAndStatus(String customerIdEvent, String status);

    @Modifying
    @Transactional
    @Query("UPDATE Bill b SET b.status= :status WHERE b.customerIdEvent= :customerIdEvent")
    void updateAllBillCustomerStatus(@Param("customerIdEvent") String customerIdEvent, @Param("status") String status);

    List<Bill> findByOrderRefAndProductIdEvent(String orderRef, String productIdEvent);

}
