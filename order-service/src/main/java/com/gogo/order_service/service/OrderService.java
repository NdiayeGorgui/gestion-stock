package com.gogo.order_service.service;

import com.gogo.base_domaine_service.constante.Constante;
import com.gogo.base_domaine_service.event.*;
import com.gogo.order_service.dto.AmountDto;
import com.gogo.order_service.dto.CustomerDto;
import com.gogo.order_service.dto.ProductStatDTO;
import com.gogo.order_service.kafka.OrderProducer;
import com.gogo.order_service.mapper.OrderMapper;
import com.gogo.order_service.model.*;
import com.gogo.order_service.repository.*;
import lombok.AllArgsConstructor;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class OrderService {

    private CustomerRepository customerRepository;
    private ProductRepository productRepository;
    private ProductItemRepository productItemRepository;
    private OrderRepository orderRepository;
    private OrderEventRepository orderEventRepository;
    private final OrderProducer orderProducer;
  

    public void saveClient(Customer customer) {
        customerRepository.save(customer);

    }

    public Order findOrderByOrderRef(String orderRef){
        return orderRepository.findByOrderIdEvent(orderRef);
    }

    public boolean existsByOrderIdEventAndOrderStatus(String id,String status){
        return orderRepository.existsByOrderIdEventAndOrderStatus(id,status);
    }

    public void saveOrderEventModel(OrderEventSourcing orderEventSourcing){
        orderEventRepository.save(orderEventSourcing);
    }

    public void saveProduit(Product product) {
        productRepository.save(product);
    }

    public void saveProductItem(ProductItem productItem) {
        productItemRepository.save(productItem);
    }

    public int qtyRestante(int quantity, int usedQuantity, String status) {
        if (status == null || quantity < 0 || usedQuantity < 0) {
            throw new IllegalArgumentException("Les paramètres ne doivent pas être nulles ou négatifs.");
        }

        if (EventStatus.valueOf(status.toUpperCase()) == EventStatus.CREATED) {
            return quantity - usedQuantity;
        }
        return quantity + usedQuantity;
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
        // Obtenir la date et l'heure actuelles
        LocalDateTime dateTime = LocalDateTime.now();
        // Extraire uniquement la date (AAAA-MM-JJ)
        String dateOnly = dateTime.toLocalDate().toString();

        savedOrder.setCustomerIdEvent(orderEvent.getCustomer().getCustomerIdEvent());
        savedOrder.setOrderIdEvent(UUID.randomUUID().toString());
        savedOrder.setOrderId(orderEvent.getCustomer().getCustomerIdEvent()+dateOnly); // todo
        savedOrder.setOrderStatus(EventStatus.PENDING.name());
        savedOrder.setDate(LocalDateTime.now());
    }

    public void createProductItem(OrderEvent orderEvent, Order savedOrder, ProductItem savedProductItem) {
        Product product=productRepository.findProductByProductIdEvent(orderEvent.getProduct().getProductIdEvent());

        savedProductItem.setProductIdEvent(orderEvent.getProduct().getProductIdEvent());
        savedProductItem.setPrice(product.getPrice());
        savedProductItem.setQuantity(orderEvent.getProductItem().getProductQty());
        savedProductItem.setOrderIdEvent(savedOrder.getOrderIdEvent());
        savedProductItem.setOrderItemId(savedOrder.getOrderId()); //todo
        savedProductItem.setDiscount(this.getAmount(savedProductItem.getQuantity(), savedProductItem.getPrice())); //todo
        Order order = orderRepository.findById(savedOrder.getId()).orElse(null);
        savedProductItem.setOrder(order);
    }

    public void sendEvent(OrderEvent orderEvent, OrderEventDto orderEventDto) {

        orderEventDto.setId(orderEvent.getOrderIdEvent());
        orderEventDto.setStatus(EventStatus.PENDING.name());
        orderEventDto.setPaymentId("Not yet"); //todo not yet

        //recuperer les infos du customer
        Customer customer=customerRepository.findCustomerByCustomerIdEvent(orderEvent.getCustomer().getCustomerIdEvent());
        orderEvent.getCustomer().setName(customer.getName());
        orderEvent.getCustomer().setPhone(customer.getPhone());
        orderEvent.getCustomer().setEmail(customer.getEmail());
        orderEvent.getCustomer().setAddress(customer.getAddress());

        //recuperer les infos du product
        Product product=productRepository.findProductByProductIdEvent(orderEvent.getProduct().getProductIdEvent());
        orderEvent.getProduct().setName(product.getName());
        orderEvent.getProduct().setCategory(product.getCategory());
        orderEvent.getProduct().setPrice(product.getPrice());
        orderEvent.getProduct().setQty(product.getQty());

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

    public void sendOrderToConfirm(String orderIdEvent){

        List<OrderEventSourcing> orders=orderEventRepository.findByOrderIdAndStatus(orderIdEvent,EventStatus.CREATED.name());
        if(!orders.isEmpty()){
            OrderEventDto orderEventDto=new OrderEventDto();
            orderEventDto.setId(orderIdEvent);
            orderEventDto.setStatus(EventStatus.CONFIRMED.name());

            Order order = this.findOrderByOrderRef(orderIdEvent);
            //save the event sourcing table with confirmed status
            OrderEventSourcing orderEventSourcing=new OrderEventSourcing();
            orderEventSourcing.setOrderId(order.getOrderIdEvent());
            orderEventSourcing.setStatus(EventStatus.CONFIRMED.name());
            orderEventSourcing.setEventTimeStamp(LocalDateTime.now());
            orderEventSourcing.setDetails("Order Confirmed");
            this.saveOrderEventModel(orderEventSourcing);

            orderProducer.sendMessage(orderEventDto);
        }
        throw new RuntimeException("Order already confirmed or not exist");
    }

    public List<ProductItem> getOrders() {
        return productItemRepository.findAll();
    }

    public List<ProductItem> getCreatedOrders(String status) {
        return productItemRepository.findByOrderOrderStatus(status);
    }

    public List<Order> findByCustomer(String customerIdEvent){
        List<Order> customerOrders=orderRepository.findByCustomerIdEvent(customerIdEvent);
        return customerOrders.stream()
                .filter(order -> order.getCustomerIdEvent().equalsIgnoreCase(customerIdEvent))
                .collect(Collectors.toList());
    }

    public void getCustomerAndProduct() {
        // Récupérer toutes les listes nécessaires
        List<Order> orders = orderRepository.findAll();
        List<Customer> customers = customerRepository.findAll();
        List<ProductItem> productItems = productItemRepository.findAll();
        List<Product> products = productRepository.findAll();

        // Créer des maps pour un accès plus rapide par clé (customerIdEvent et productIdEvent)
        Map<String, Customer> customerMap = customers.stream()
                .collect(Collectors.toMap(Customer::getCustomerIdEvent, customer -> customer));

        Map<String, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getProductIdEvent, product -> product));

        // Associer les clients aux commandes
        orders.forEach(order ->
                order.setCustomer(customerMap.get(order.getCustomerIdEvent()))
        );

        // Associer les produits aux ProductItems
        productItems.forEach(productItem ->
                productItem.setProduct(productMap.get(productItem.getProductIdEvent()))
        );
    }

    public ProductItem getOrderById(Long id) {
        return productItemRepository.findById(id).orElse(null);
    }

    public List<ProductItem> getOrderById(String id) {
        return productItemRepository.findByOrderCustomerIdEvent(id);
    }

    public List<ProductItem> findByOrderCustomerIdEventAndStatus(String id,String status){
        return productItemRepository.findByOrderCustomerIdEventAndOrderOrderStatus( id, status);
    }

    public AmountDto getAmount(String customerIdEvent, String status) {
        List<ProductItem> orders = this.findByOrderCustomerIdEventAndStatus(customerIdEvent, status);
        AmountDto amountDto = new AmountDto();
        double amount = orders.stream()
                .map(ProductItem::getAmount)
                .mapToDouble(i -> i).sum();
        double discount = this.getDiscount(customerIdEvent, status);
        amountDto.setDiscount(discount);
        amountDto.setTax((amount + discount) * Constante.TAX);
        amountDto.setAmount(amount + discount);
        amountDto.setTotalAmount((amount + discount) * Constante.TAX + amount);

        return amountDto;
    }

    public  double getDiscount(String customerIdEvent,String status){
        List<ProductItem> orders=this.findByOrderCustomerIdEventAndStatus(customerIdEvent,status);
        return orders.stream()
                .map(ProductItem::getDiscount)
                .mapToDouble(i->i).sum();
    }

    public List<Customer> getCustomerById(String id) {
        List<Customer> customers = customerRepository.findAll();
        return customers.stream()
                .filter(cus -> cus.getCustomerIdEvent().equalsIgnoreCase(id))
                .collect(Collectors.toList());
    }

    public double getAmount(int qty, double price) {
        double total = qty * price;
        return (total < 100) ? 0 : (total < 200) ? 0.005 * total : 0.01 * total;
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

    public Customer findCustomerById(String customerIdEvent) {
        return customerRepository.findCustomerByCustomerIdEvent(customerIdEvent);
    }

    public ProductItem findProductItemByOrderEventId(String orderEventId) {
        return productItemRepository.findByOrderIdEvent(orderEventId);
    }

   public List<OrderEventSourcing> orderEventSourcingList(String status,String id){
       return orderEventRepository.findByStatusAndCustomerId(status,id);
   }

    public boolean isOrderAlreadyProcessed(String orderId) {
        List<OrderEventSourcing> events = orderEventRepository.findByOrderIdAndStatusIn(
                orderId,
                Arrays.asList(EventStatus.CONFIRMED.name(), EventStatus.CANCELED.name(), EventStatus.SHIPPED.name(),EventStatus.DELIVERED.name())
        );
        return !events.isEmpty(); // Retourne true si la commande est déjà traitée
    }

	public List<OrderEventSourcing> getOrderEvents() {
		
		return orderEventRepository.findAll();
	}
	
	public List<ProductStatDTO> getMostOrderedProducts() {
        List<Object[]> results = productItemRepository.findMostOrderedProductIds();

        return results.stream().map(r -> {
            String productIdEvent = (String) r[0];
            Long totalQuantite = ((Number) r[1]).longValue();

            // Récupérer le produit via la méthode existante
            Product product = productRepository.findProductByProductIdEvent(productIdEvent);
            String name = (product != null) ? product.getName() : "Produit inconnu";

            return new ProductStatDTO(productIdEvent, name, totalQuantite);
        }).collect(Collectors.toList());
    }
	
	public List<CustomerDto> getTop10Customers() {
        List<Object[]> results = orderRepository.findTop10CustomersByOrderCount();
        
        return results.stream()
            .map(result -> {
                String customerIdEvent = (String) result[0];
                Long totalOrder = ((Number) result[1]).longValue();

                // Récupérer les infos du client
                Customer customer = customerRepository.findCustomerByCustomerIdEvent(customerIdEvent);

                // Vérifier si le client existe avant de retourner le DTO
                if (customer != null) {
                    return new CustomerDto(customer.getCustomerIdEvent(), customer.getName(), totalOrder);
                } else {
                    return new CustomerDto(customerIdEvent, "Inconnu", totalOrder);
                }
            })
            .collect(Collectors.toList());
    }

}

