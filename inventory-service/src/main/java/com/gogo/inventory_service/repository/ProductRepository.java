package com.gogo.inventory_service.repository;

import com.gogo.inventory_service.model.ProductModel;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<ProductModel,Long> {

    ProductModel findProductByProductIdEvent(String productIdEvent);
    @Modifying
    @Query("UPDATE ProductModel p SET p.status= :status WHERE p.productIdEvent= :productIdEvent")
    void updateProductStatus(@Param("productIdEvent") String productIdEvent, @Param("status") String status);

    @Transactional
    @Modifying
    @Query("UPDATE ProductModel p SET p.qtyStatus= :qtyStatus WHERE p.productIdEvent= :productIdEvent")
    void updateProductQtyStatus(@Param("productIdEvent") String productIdEvent, @Param("qtyStatus") String qtyStatus);

    @Modifying
    @Transactional
    @Query("DELETE FROM ProductModel p  where p.productIdEvent =:productIdEvent")
    void deleteProduct(@Param("productIdEvent") String customerIdEvent,@Param("productIdEvent") String status);

    @Modifying
    @Transactional
    @Query("UPDATE ProductModel p SET p.status= :status,  p.name= :name, p.category= :category, p.description= :description,  p.location= :location,  p.qty= :qty,  p.price= :price,p.qtyStatus= :qtyStatus WHERE p.productIdEvent= :productIdEvent")
    int updateProduct(@Param("productIdEvent") String productIdEvent, @Param("status") String status, @Param("name") String name, @Param("category") String category,@Param("description") String description,@Param("location") String location, @Param("qty") int qty, @Param("price") double price,@Param("qtyStatus") String qtyStatus);

    @Modifying
    @Transactional
    @Query("UPDATE ProductModel p SET p.qty= :qty WHERE p.productIdEvent= :productIdEvent")
    void updateQuantity(@Param("productIdEvent") String productIdEvent, @Param("qty") int qty);

}
