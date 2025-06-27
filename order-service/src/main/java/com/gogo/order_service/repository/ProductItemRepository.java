package com.gogo.order_service.repository;

import com.gogo.order_service.dto.OrderResponseDto;
import com.gogo.order_service.model.ProductItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductItemRepository extends JpaRepository<ProductItem,Long> {
    List<OrderResponseDto> findByOrderCustomerIdEvent(String id);
    List<ProductItem> findByOrderCustomerIdEventAndOrderOrderStatus(String id,String status);
    List<ProductItem> findByOrderOrderStatus(String status);
    ProductItem findByOrderIdEvent(String id);

    @Query("SELECT pi.productIdEvent, SUM(pi.quantity) AS totalQuantite " +
            "FROM ProductItem pi " +
            "GROUP BY pi.productIdEvent " +
            "ORDER BY totalQuantite DESC")
     List<Object[]> findMostOrderedProductIds();


    List<ProductItem> findByOrderOrderIdEvent(String orderIdEvent);

    List<ProductItem> findByOrderOrderIdEventIn(List<String> orderIds);

    ProductItem findProductItemByOrderIdEventAndProductIdEvent(String orderIdEvent, String productIdEvent);

    List<ProductItem> findByOrder_OrderIdEvent(String orderIdEvent);
}
