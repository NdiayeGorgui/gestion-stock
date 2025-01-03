package com.gogo.inventory_service.controller;


import com.gogo.base_domaine_service.dto.Product;
import com.gogo.inventory_service.exception.ProductNotFoundException;
import com.gogo.inventory_service.kafka.ProductProducer;
import com.gogo.inventory_service.model.ProductModel;
import com.gogo.inventory_service.service.ProductService;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1")
public class ProductController {

    private final ProductProducer productProducer;
    private final ProductService productService;

    public ProductController(ProductProducer productProducer,ProductService productService) {
        this.productProducer = productProducer;
        this.productService=productService;
    }

    @PostMapping("/products")
    public String saveAndSendProduct(@RequestBody Product product){

       productService.saveAndSendProduct(product);
       return "Product sent successfully ...";
    }

    @DeleteMapping("/products/{productIdEvent}")
    public String sendCustomer(@PathVariable String productIdEvent) throws ProductNotFoundException {
        ProductModel productModel=productService.findProductById(productIdEvent);
        if(productModel!=null){
            productService.sendProductToDelete(productIdEvent);
            return "Product sent successfully ...";
        }
        throw new ProductNotFoundException("Product not available with id: " + productIdEvent);
    }

    @PutMapping("/products/{productIdEvent}")
    public String updateAndSendProduct(@RequestBody Product product,@PathVariable("productIdEvent") String productIdEvent) throws ProductNotFoundException {
        ProductModel productModel=productService.findProductById(productIdEvent);
        if(productModel!=null){
            productService.sendProductToUpdate(productIdEvent,product);
            return "Product sent successfully ...";
        }
        throw new ProductNotFoundException("Product not available with id: " + productIdEvent);
    }
}
