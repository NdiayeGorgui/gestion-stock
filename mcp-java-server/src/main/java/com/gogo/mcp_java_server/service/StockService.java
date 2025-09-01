package com.gogo.mcp_java_server.service;

import com.gogo.base_domaine_service.event.EventStatus;
import com.gogo.mcp_java_server.model.*;
import com.gogo.mcp_java_server.repository.*;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
@EnableScheduling
@Service
public class StockService {
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final DeliveredRepository deliveredRepository;
    private final ShipRepository shipRepository;
    private final BillRepository billRepository;

    public StockService(CustomerRepository customerRepository, ProductRepository productRepository, OrderRepository orderRepository, DeliveredRepository deliveredRepository, ShipRepository shipRepository, BillRepository billRepository) {
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.deliveredRepository = deliveredRepository;
        this.shipRepository = shipRepository;
        this.billRepository = billRepository;
    }

  public List<Customer> getAllCustomers(){
        return customerRepository.findAll();
  }
  public List<Product> getAllProducts(){
        return productRepository.findAll();
  }

    public List<Bill> getAllOrderItems(){
        return billRepository.findAll();
    }

  public List<Order> getAllOrders(){
        return orderRepository.findAll();
  }
  public List<Delivered> getAllDelivers(){
        return deliveredRepository.findAll();
  }
  public List<Ship> getAllShips(){
        return shipRepository.findAll();
  }

    public void saveBill(Bill bill){
        billRepository.save(bill);
    }

    public Bill findByOrderIdAndProductId(String orderRef, String productId) {
        return billRepository.findByOrderRefAndProductId(orderRef,productId);
    }

    public void updateTheBillStatus(String orderRef, String status) {
        billRepository.updateTheBillStatus(orderRef, status);
    }

    public void saveCustomer(Customer customer) {
        customerRepository.save(customer);
    }

    public void saveProduct(Product product) {
        productRepository.save(product);
    }


    @Scheduled(fixedRate = 15000)
    public void productAvailable() {
        List<Product> producList = productRepository.findAll();

        for (Product productModel : producList) {
            int qty = productModel.getQty();
            String currentStatus = productModel.getQtyStatus();
            String newStatus ="";

            // Déterminer le nouveau statut basé uniquement sur la quantité
            if (qty == 0) {
                newStatus = EventStatus.UNAVAILABLE.name();
            } else if (qty < 10) {
                newStatus = EventStatus.LOW.name();
            } else {
                newStatus = EventStatus.AVAILABLE.name();
            }

            // Mettre à jour uniquement si le statut a changé
            if (!newStatus.equals(currentStatus)) {
                productModel.setQtyStatus(newStatus);
                productRepository.updateProductQtyStatus(productModel.getProductId(), newStatus);
            }

        }
    }
}
