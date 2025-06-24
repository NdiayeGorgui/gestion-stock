package com.gogo.inventory_service.mapper;


import com.gogo.base_domaine_service.dto.Product;
import com.gogo.base_domaine_service.event.EventStatus;
import com.gogo.inventory_service.dto.ProductNumberGenerator;
import com.gogo.inventory_service.model.ProductModel;

import java.time.LocalDateTime;
import java.util.UUID;

public class ProductMapper {



    public static ProductModel mapToProductModel(Product product) {
        String productId = ProductNumberGenerator.generateProductNumber();
        return new ProductModel(
                null,
                productId,
                product.getName(),
                product.getCategory(),
                product.getDescription(),
                product.getLocation(),
                product.getQty(),
                product.getPrice(),
                EventStatus.PENDING.name(),
                product.getQty() >= 10 ? EventStatus.AVAILABLE.name() : EventStatus.LOW.name(),
                LocalDateTime.now()
        );
    }


    public static Product mapToProduct(ProductModel productModel){

        return new Product(
                productModel.getProductIdEvent(),
                productModel.getName(),
                productModel.getCategory(),
                productModel.getDescription(),
                productModel.getLocation(),
                productModel.getQty(),
                productModel.getPrice(),
                productModel.getQtyStatus()
        );
    }
    }

