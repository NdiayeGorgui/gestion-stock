package com.gogo.inventory_service.service;

import com.gogo.base_domaine_service.dto.Product;
import com.gogo.base_domaine_service.event.EventStatus;
import com.gogo.base_domaine_service.event.ProductEvent;
import com.gogo.inventory_service.kafka.ProductProducer;
import com.gogo.inventory_service.mapper.ProductMapper;
import com.gogo.inventory_service.model.ProductModel;
import com.gogo.inventory_service.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@EnableScheduling
@Service
public class ProductService {

    private final ProductRepository productRepository;

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
        existingProduct.setCategory(product.getCategory());
        existingProduct.setPrice(product.getPrice());
        existingProduct.setQty(product.getQty());

        productRepository.save(existingProduct);
    }

    public void saveAndSendProduct(Product product){
        ProductModel savedProduct= ProductMapper.mapToProductModel(product);
        this.saveProduct(savedProduct);

        product.setProductIdEvent(savedProduct.getProductIdEvent());
        ProductEvent productEvent = new ProductEvent();

        productEvent.setStatus(EventStatus.PENDING.name());
        productEvent.setMessage("Product status is in pending state");
        productEvent.setProduct(product);

        productProducer.sendMessage(productEvent);
    }

    public void updateAndSendProduct(Long id,Product product){
        ProductModel existingProduct=productRepository.findById(id).get();

        existingProduct.setName(product.getName());
        existingProduct.setCategory(product.getCategory());
        existingProduct.setPrice(product.getPrice());
        existingProduct.setQty(product.getQty());
        this.saveProduct(existingProduct);

        ProductEvent productEvent = new ProductEvent();
        productEvent.setStatus(EventStatus.PENDING.name());
        productEvent.setMessage("Product status is in pending state");
        productEvent.setProduct(product);

        productProducer.sendMessage(productEvent);
    }

    public void sendProductToDelete(String productIdEvent){
        ProductModel productModel=productRepository.findProductByProductIdEvent(productIdEvent);

        Product product=ProductMapper.mapToProduct(productModel);

        ProductEvent productEvent=new ProductEvent();

        productEvent.setStatus(EventStatus.DELETING.name());
        productEvent.setMessage("Product status is in deleting state");
        productEvent.setProduct(product);

        productProducer.sendMessage(productEvent);
    }

    public void sendProductToUpdate(String productIdEvent, Product product){
        ProductModel productModel=productRepository.findProductByProductIdEvent(productIdEvent);

        product.setProductIdEvent(productModel.getProductIdEvent());

        ProductEvent productEvent=new ProductEvent();

        productEvent.setStatus(EventStatus.UPDATING.name());
        productEvent.setMessage("Product status is in updating state");
        productEvent.setProduct(product);

        productProducer.sendMessage(productEvent);
    }

    @Transactional
    public void updateProductStatus(String productIdEvent, String status ){
        productRepository.updateProductStatus(productIdEvent, status);

    }

    public void updateProduct(String productIdEvent, String status, String name,String category, int qty, double price, String qtyStatus ){
        productRepository.updateProduct(productIdEvent, status, name,category, qty, price, qtyStatus);

    }
    public void updateProductQty(String productIdEvent, int qty ){
        productRepository.updateQuantity(productIdEvent, qty);

    }
    public void deleteProduct(String productIdEvent,String status ){
        productRepository.deleteProduct(productIdEvent,status);

    }

    public int qtyRestante(int quantity, int usedQuantity, String status) {
        if (status.equalsIgnoreCase(EventStatus.CREATED.name()))
            return (quantity - usedQuantity);
        else
            return (quantity + usedQuantity);
    }
    public ProductModel findProductById(String id){
      return   productRepository.findProductByProductIdEvent(id);
    }

    @Scheduled(fixedRate = 60000)
    public void productAvailable(){
        List<ProductModel> productModelList=productRepository.findAll();
        Product product=new Product();
        ProductEvent productEvent=new ProductEvent();

        productModelList.forEach(productModel -> {
            if(productModel.getQty()==0 && productModel.getQtyStatus().equalsIgnoreCase(EventStatus.AVAILABLE.name())){
                productModel.setQtyStatus(EventStatus.UNAVAILABLE.name());
                product.setQtyStatus(productModel.getQtyStatus());
                product.setProductIdEvent(productModel.getProductIdEvent());
                productEvent.setStatus(EventStatus.UNAVAILABLE.name());
                productEvent.setProduct(product);

                productRepository.updateProductQtyStatus(productModel.getProductIdEvent(),productModel.getQtyStatus());
                productProducer.sendMessage(productEvent);
            }
            log.info("ProductId : {} , Quantity : {}, Quantity Status : {}",productModel.getProductIdEvent(),productModel.getQty(),productModel.getQtyStatus());
        });
    }

    public List<ProductModel> getAllProducts() {
        return productRepository.findAll();
    }

    public ProductModel getProduct(String id){
        return productRepository.findProductByProductIdEvent(id);
    }
}
