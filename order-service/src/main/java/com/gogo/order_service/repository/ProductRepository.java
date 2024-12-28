package com.gogo.order_service.repository;

import com.gogo.order_service.model.Product;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product,Long> {
    boolean existsByProductIdEventAndStatus(String productIdEvent,String productStatus);
    Product findProductByProductIdEvent(String id);
    @Modifying
    @Transactional
    @Query("DELETE FROM Product p  where p.productIdEvent =:productIdEvent")
    void deleteProduct(@Param("productIdEvent") String productIdEvent);
    @Modifying
    @Transactional
    @Query("UPDATE Product p SET p.status= :status,  p.name= :name,  p.qty= :qty,  p.price= :price,p.qtyStatus= :qtyStatus WHERE p.productIdEvent= :productIdEvent")
    int updateProduct(@Param("productIdEvent") String productIdEvent, @Param("status") String status, @Param("name") String name, @Param("qty") int qty, @Param("price") double price,@Param("qtyStatus") String qtyStatus);

    @Modifying
    @Transactional
    @Query("UPDATE Product p SET p.qty= :qty WHERE p.productIdEvent= :productIdEvent")
    int updateQuantity(@Param("productIdEvent") String productIdEvent, @Param("qty") int qty);

    @Transactional
    @Modifying
    @Query("UPDATE Product p SET p.qtyStatus= :qtyStatus WHERE p.productIdEvent= :productIdEvent")
    int updateProductQtyStatus(String productIdEvent, String qtyStatus);
}
