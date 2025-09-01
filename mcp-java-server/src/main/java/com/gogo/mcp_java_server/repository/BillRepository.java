package com.gogo.mcp_java_server.repository;


import com.gogo.mcp_java_server.model.Bill;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BillRepository extends JpaRepository <Bill,Long>{
     Bill  findBillByOrderRef(String orderRef);
    List<Bill>  findByOrderRef(String orderRef);
    @Modifying
    @Transactional
    @Query("UPDATE Bill b SET b.status= :status WHERE b.orderRef= :orderRef")
    void updateTheBillStatus(@Param("orderRef") String orderRef, @Param("status") String status);

    List<Bill> findByCustomerIdAndStatus(String customerId, String status);

    @Modifying
    @Transactional
    @Query("UPDATE Bill b SET b.status= :status WHERE b.customerId= :customerId")
    void updateAllBillCustomerStatus(@Param("customerId") String customerId, @Param("status") String status);

    List<Bill> findByCustomerId(String customerId);
    List<Bill> findByOrderRefAndStatus(String orderRef, String status);

    boolean existsByOrderRefAndStatus(String orderRef, String status);

    Bill findByOrderRefAndProductId(String orderRef, String productId);

    @Transactional
    @Modifying
    @Query("UPDATE Bill b SET b.status = :status WHERE b.orderRef = :orderRef")
    void updateAllBillOrderStatus(@Param("orderRef") String orderRef, @Param("status") String status);



    //List<Bill> findByOrderId(String orderId);
}
