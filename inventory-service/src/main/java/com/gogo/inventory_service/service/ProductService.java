package com.gogo.inventory_service.service;

import com.gogo.base_domaine_service.dto.Product;
import com.gogo.base_domaine_service.event.ProductEvent;
import com.gogo.inventory_service.kafka.ProductProducer;
import com.gogo.inventory_service.model.ProductModel;
import com.gogo.inventory_service.repository.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ProductService {

    ProductRepository productRepository;

    private final ProductProducer productProducer;


    public ProductService(ProductProducer productProducer,ProductRepository productRepository) {
        this.productProducer = productProducer;
        this.productRepository=productRepository;
    }

    public void saveProduct(ProductModel product){
        productRepository.save(product);
    }

    public void updateProduct(Long id,ProductModel product){
        ProductModel existingProduct=productRepository.findById(id).get();

        existingProduct.setName(product.getName());
        existingProduct.setPrice(product.getPrice());
        existingProduct.setQty(product.getQty());

        productRepository.save(existingProduct);
    }

    public void saveAndSendProduct(Product product){
        ProductModel savedProduct=new ProductModel();
        savedProduct.setProductIdEvent(UUID.randomUUID().toString());
        savedProduct.setName(product.getName());
        savedProduct.setQty(product.getQty());
        savedProduct.setPrice(product.getPrice());
        savedProduct.setStatus("PENDING");
        savedProduct.setQtyStatus("AVAILABLE");
        this.saveProduct(savedProduct);

        product.setId(savedProduct.getProductIdEvent());
        ProductEvent productEvent = new ProductEvent();
       // productEvent.setEventType("CreateProduct");
        productEvent.setStatus("PENDING");
        productEvent.setMessage("Product status is in pending state");
        productEvent.setProduct(product);

        productProducer.sendMessage(productEvent);
    }

    public void updateAndSendProduct(Long id,Product product){
        ProductModel existingProduct=productRepository.findById(id).get();

        existingProduct.setName(product.getName());
        existingProduct.setPrice(product.getPrice());
        existingProduct.setQty(product.getQty());
        this.saveProduct(existingProduct);

       // product.setId(UUID.randomUUID().toString());
        ProductEvent productEvent = new ProductEvent();
       // productEvent.setEventType("UpdateProduct");
        productEvent.setStatus("PENDING");
        productEvent.setMessage("Product status is in pending state");
        productEvent.setProduct(product);

        productProducer.sendMessage(productEvent);
    }

    public void sendProductToDelete(String productIdEvent){
        ProductModel productModel=productRepository.findProductByProductIdEvent(productIdEvent);
        Product product=new Product();
        product.setId(productModel.getProductIdEvent());
        product.setName(productModel.getName());
        product.setQty(productModel.getQty());
        product.setPrice(productModel.getPrice());

        ProductEvent productEvent=new ProductEvent();

        productEvent.setStatus("DELETING");
        productEvent.setMessage("Product status is in deleting state");
        productEvent.setProduct(product);

        productProducer.sendMessage(productEvent);

    }

    public void sendProductToUpdate(String productIdEvent, Product product){
        ProductModel productModel=productRepository.findProductByProductIdEvent(productIdEvent);

        product.setId(productModel.getProductIdEvent());


        ProductEvent productEvent=new ProductEvent();

        productEvent.setStatus("UPDATING");
        productEvent.setMessage("Product status is in updating state");
        productEvent.setProduct(product);

        productProducer.sendMessage(productEvent);

    }

    @Transactional
    public int updateProductStatus(String productIdEvent,String status ){
        return productRepository.updateProductStatus(productIdEvent,status);

    }

    public int updateProduct(String productIdEvent,String status,String name,int qty, double price ){
        return productRepository.updateProduct(productIdEvent,status,name,qty,price);

    }
    public int updateProductQty(String productIdEvent,int qty ){
        return productRepository.updateQuantity(productIdEvent,qty);

    }
    public void deleteProduct(String productIdEvent,String status ){
        productRepository.deleteProduct(productIdEvent,status);

    }

    public int qtyRestante(int quantity, int usedQuantity){
        return (quantity-usedQuantity);

    }
    public ProductModel findProductById(String id){
      return   productRepository.findProductByProductIdEvent(id);
    }
}
