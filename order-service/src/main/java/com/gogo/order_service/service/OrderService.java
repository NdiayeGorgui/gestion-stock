package com.gogo.order_service.service;

import com.gogo.base_domaine_service.event.*;
import com.gogo.order_service.kafka.OrderProducer;
import com.gogo.order_service.mapper.OrderMapper;
import com.gogo.order_service.model.*;
import com.gogo.order_service.repository.CustomerRepository;
import com.gogo.order_service.repository.OrderRepository;
import com.gogo.order_service.repository.ProductItemRepository;
import com.gogo.order_service.repository.ProductRepository;
import lombok.AllArgsConstructor;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class OrderService {

    private CustomerRepository customerRepository;
    private ProductRepository productRepository;
    private ProductItemRepository productItemRepository;
    private OrderRepository orderRepository;
    private final OrderProducer orderProducer;

    public void saveClient(Customer customer) {
        customerRepository.save(customer);

    }

    public void saveProduit(Product product) {
        productRepository.save(product);
    }

    public void saveProductItem(ProductItem productItem) {
        productItemRepository.save(productItem);
    }

    public int qtyRestante(int quantity, int usedQuantity, String status) {
        if (status.equalsIgnoreCase(EventStatus.CREATED.name()))
            return (quantity - usedQuantity);
        else
            return (quantity + usedQuantity);
    }

    public void updateQuantity(String productIdEvent, int qty) {
        productRepository.updateQuantity(productIdEvent, qty);
    }

    public void saveOrder(Order order) {
        orderRepository.save(order);
    }

    public List<Customer> getCustomers() {
        return customerRepository.findAll();
    }

    public List<Product> getProducts() {
        return productRepository.findAll();
    }

    public void createOrder(OrderEvent orderEvent, Order savedOrder) {

        savedOrder.setCustomerIdEvent(orderEvent.getCustomer().getId());
        savedOrder.setOrderIdEvent(UUID.randomUUID().toString());
        savedOrder.setOrderStatus(EventStatus.PENDING.name());
        savedOrder.setDate(LocalDateTime.now());
    }

    public void createProductItem(OrderEvent orderEvent, Order savedOrder, ProductItem savedProductItem) {

        savedProductItem.setProductIdEvent(orderEvent.getProduct().getId());
        savedProductItem.setPrice(orderEvent.getProduct().getPrice());
        savedProductItem.setQuantity(orderEvent.getProductItem().getProductQty());
        savedProductItem.setOrderIdEvent(savedOrder.getOrderIdEvent());
        savedProductItem.setDiscount(this.getAmount(savedProductItem.getQuantity(), savedProductItem.getPrice())); //todo
        Order order = orderRepository.findById(savedOrder.getId()).orElse(null);
        savedProductItem.setOrder(order);
    }

    public void sendEvent(OrderEvent orderEvent, OrderEventDto orderEventDto) {

        orderEventDto.setId(orderEvent.getOrderIdEvent());
        orderEventDto.setStatus(EventStatus.PENDING.name());
        orderEventDto.setName("Order");

        CustomerEventDto customerEventDto = OrderMapper.mapToCustomerEventDto(orderEvent);
        ProductEventDto productEventDto = OrderMapper.mapToProductEventDto(orderEvent);
        ProductItemEventDto productItemEventDto = OrderMapper.mapToProductItemEventDto(orderEvent);

        List<ProductItem> productItems = productItemRepository.findAll();
        List<Order> orders = orderRepository.findAll();
        for (Order order : orders) {
            for (ProductItem productItem : productItems) {
                if (productItem.getOrder().getOrderIdEvent().equalsIgnoreCase(order.getOrderIdEvent())) {
                    productItemEventDto.setDiscount(productItem.getDiscount());
                }
            }
        }

        orderEventDto.setProductEventDto(productEventDto);
        orderEventDto.setCustomerEventDto(customerEventDto);
        orderEventDto.setProductItemEventDto(productItemEventDto);
    }

    public void sendOrderToCancel(String orderIdEvent){
       // Order order=orderRepository.findByOrderIdEvent(orderIdEvent);

        OrderEventDto orderEventDto=new OrderEventDto();
        orderEventDto.setId(orderIdEvent);
        orderEventDto.setStatus(EventStatus.CANCELLING.name());
        orderProducer.sendMessage(orderEventDto);
    }

    public List<ProductItem> getOrders() {
        return productItemRepository.findAll();
    }

    public void getCustomerAndProduct() {
        List<Order> orders = orderRepository.findAll();
        List<Customer> customers = customerRepository.findAll();
        List<ProductItem> productItems = productItemRepository.findAll();
        List<Product> products = productRepository.findAll();

        for (Order order : orders) {
            for (Customer customer : customers) {
                if (customer.getCustomerIdEvent().equalsIgnoreCase(order.getCustomerIdEvent())) {
                    order.setCustomer(customer);

                }
            }
        }
        for (ProductItem productItem : productItems) {
            for (Product product : products) {
                if (product.getProductIdEvent().equalsIgnoreCase(productItem.getProductIdEvent())) {
                    productItem.setProduct(product);
                }
            }
        }
    }

    public ProductItem getOrderById(Long id) {

        return productItemRepository.findById(id).orElse(null);
    }

    public List<ProductItem> getOrderById(String id) {

        return productItemRepository.findByOrderCustomerIdEvent(id);
    }

    public List<Customer> getCustomerById(String id) {
        List<Customer> customers = customerRepository.findAll();
        return customers.stream()
                .filter(cus -> cus.getCustomerIdEvent().equalsIgnoreCase(id))
                .collect(Collectors.toList());
    }

    public double getAmount(int qty, double price) {
        double total = qty * price;
        double amount = 0;
        if (total < 100) {
            amount = 0;
        } else if (total >= 100 && total < 200) {
            amount = 0.005 * total;
        } else {
            amount = 0.01 * total;
        }
        return amount;
    }

    public void updateOrderStatus(String oderIdEvent, String status ){
        orderRepository.updateOrderStatus(oderIdEvent, status);

    }

    public void updateAllOrderStatus(String customerIdEvent,String status ){
         orderRepository.updateAllOrderStatus(customerIdEvent,status);

    }

    public Product findProductById(String productIdEvent) {
        return productRepository.findProductByProductIdEvent(productIdEvent);
    }

    public ProductItem findProductItemByOrderEventId(String orderEventId) {
        return productItemRepository.findByOrderIdEvent(orderEventId);
    }
}
