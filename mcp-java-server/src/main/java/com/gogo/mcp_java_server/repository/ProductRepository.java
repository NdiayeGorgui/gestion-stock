package com.gogo.mcp_java_server.repository;

import com.gogo.mcp_java_server.model.Product;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product,Long> {

    boolean existsByProductIdAndStatus(String productId,String productStatus);
    Product findProductByProductId(String id);
    @Modifying
    @Transactional
    @Query("DELETE FROM Product p  where p.productId =:productId")
    void deleteProduct(@Param("productId") String productId);

    @Modifying
    @Transactional
    @Query("UPDATE Product p SET p.status= :status,  p.name= :name, p.category= :category, p.description= :description,p.location= :location, p.qty= :qty,  p.price= :price,p.qtyStatus= :qtyStatus WHERE p.productId= :productId")
    void updateProduct(@Param("productId") String productId, @Param("status") String status, @Param("name") String name, @Param("category") String category,@Param("description") String description,@Param("location") String location, @Param("qty") int qty, @Param("price") double price,@Param("qtyStatus") String qtyStatus);

    @Modifying
    @Transactional
    @Query("UPDATE Product p SET p.qty= :qty WHERE p.productId= :productId")
    void updateQuantity(@Param("productId") String productId, @Param("qty") int qty);

    @Transactional
    @Modifying
    @Query("UPDATE Product p SET p.qtyStatus = :qtyStatus WHERE p.productId = :productId")
    void updateProductQtyStatus(@Param("productId") String productId,
                                @Param("qtyStatus") String qtyStatus);
}
