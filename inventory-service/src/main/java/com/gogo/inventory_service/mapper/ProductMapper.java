package com.gogo.inventory_service.mapper;


import com.gogo.base_domaine_service.dto.Product;
import com.gogo.base_domaine_service.event.EventStatus;
import com.gogo.inventory_service.model.ProductModel;

import java.util.UUID;

public class ProductMapper {



        public static ProductModel mapToProductModel(Product product){

            return new ProductModel(
                    null,
                    UUID.randomUUID().toString(),
                    product.getName(),
                    product.getQty(),
                    product.getPrice(),
                    EventStatus.PENDING.name(),
                    EventStatus.AVAILABLE.name()
            );
        }

    public static Product mapToProduct(ProductModel productModel){

        return new Product(
                productModel.getProductIdEvent(),
                productModel.getName(),
                productModel.getQty(),
                productModel.getPrice(),
                productModel.getQtyStatus()
        );
    }
    }

