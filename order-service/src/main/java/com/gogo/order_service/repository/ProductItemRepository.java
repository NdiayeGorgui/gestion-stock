package com.gogo.order_service.repository;

import com.gogo.order_service.model.ProductItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductItemRepository extends JpaRepository<ProductItem,Long> {
    List<ProductItem> findByOrderCustomerIdEvent(String id);
    List<ProductItem> findByOrderCustomerIdEventAndOrderOrderStatus(String id,String status);
    List<ProductItem> findByOrderOrderStatus(String status);
    ProductItem findByOrderIdEvent(String id);


}
