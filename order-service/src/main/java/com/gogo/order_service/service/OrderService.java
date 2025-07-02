package com.gogo.order_service.service;

import com.gogo.base_domaine_service.constante.Constante;
import com.gogo.base_domaine_service.event.*;
import com.gogo.order_service.dto.*;
import com.gogo.order_service.kafka.OrderProducer;
import com.gogo.order_service.mapper.OrderMapper;
import com.gogo.order_service.model.*;
import com.gogo.order_service.repository.*;
import lombok.AllArgsConstructor;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@EnableScheduling
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

   /* public void createOrder(OrderEvent orderEvent, Order savedOrder) {
        // Obtenir la date et l'heure actuelles
        LocalDateTime dateTime = LocalDateTime.now();
        // Extraire uniquement la date (AAAA-MM-JJ)
        String dateOnly = dateTime.toLocalDate().toString();

        savedOrder.setCustomerIdEvent(orderEvent.getCustomer().getCustomerIdEvent());
        savedOrder.setOrderIdEvent(UUID.randomUUID().toString());
        savedOrder.setOrderId(orderEvent.getCustomer().getCustomerIdEvent()+dateOnly); // todo
        savedOrder.setOrderStatus(EventStatus.PENDING.name());
        savedOrder.setDate(LocalDateTime.now());
    }*/

   /* public void createProductItem(OrderEvent orderEvent, Order savedOrder, ProductItem savedProductItem) {
        Product product=productRepository.findProductByProductIdEvent(orderEvent.getProduct().getProductIdEvent());

        savedProductItem.setProductIdEvent(orderEvent.getProduct().getProductIdEvent());
        savedProductItem.setPrice(product.getPrice());
        savedProductItem.setQuantity(orderEvent.getProductItem().getProductQty());
        savedProductItem.setOrderIdEvent(savedOrder.getOrderIdEvent());
        savedProductItem.setOrderItemId(savedOrder.getOrderId()); //todo
        savedProductItem.setDiscount(this.getAmount(savedProductItem.getQuantity(), savedProductItem.getPrice())); //todo
        Order order = orderRepository.findById(savedOrder.getId()).orElse(null);
        savedProductItem.setOrder(order);
    }*/

    public Order createOrder(CommandEvent commandEvent) {
        LocalDateTime now = LocalDateTime.now();
        String orderIdEvent = OrderNumberGenerator.generateOrderNumber();

        Order order = new Order();
        order.setCustomerIdEvent(commandEvent.getCustomer().getCustomerIdEvent());
        order.setOrderIdEvent(orderIdEvent);
        order.setOrderStatus(EventStatus.PENDING.name());
        order.setDate(now);

        return orderRepository.save(order);
    }



    public void createProductItem(ProductItemRequest itemRequest, Product product, Order order, ProductItem item) {
        item.setProductIdEvent(product.getProductIdEvent());
        item.setPrice(product.getPrice());
        item.setQuantity(itemRequest.getProductQty());
        item.setDiscount(this.getAmount(item.getQuantity(), item.getPrice()));
        item.setOrder(order);
        item.setOrderIdEvent(order.getOrderIdEvent());
        // item.setOrderItemId(order.getOrderIdEvent()); // même ID que l'order
    }


    /* public void sendEvent(OrderEvent orderEvent, OrderEventDto orderEventDto) {

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

         orderEvent.getProduct().setQtyStatus(product.getQtyStatus());

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
     }*/
    public void createProductItemFromRequest(ProductItemRequest request, Product product, Order order, ProductItem item) {
        item.setProductIdEvent(request.getProductIdEvent());
        item.setPrice(product.getPrice());
        item.setQuantity(request.getProductQty());
        item.setDiscount(calculateDiscount(item.getQuantity(), item.getPrice()));
        item.setOrderIdEvent(order.getOrderIdEvent());
        item.setOrder(order);
    }
    private double calculateDiscount(int qty, double price) {
        double total = qty * price;
        if (total < 100) return 0;
        else if (total < 200) return 0.005 * total;
        else return 0.01 * total;
    }

    public OrderEventDto buildEventDto(Order order, List<ProductItem> productItems, String username) {
        OrderEventDto dto = new OrderEventDto();

        dto.setId(order.getOrderIdEvent());
        dto.setStatus(order.getOrderStatus());
        dto.setUserName(username);

        // === Customer ===
        Customer customer = customerRepository.findCustomerByCustomerIdEvent(order.getCustomerIdEvent());
        CustomerEventDto customerDto = new CustomerEventDto(
                customer.getCustomerIdEvent(),
                customer.getStatus(),
                customer.getName(),
                customer.getPhone(),
                customer.getEmail(),
                customer.getAddress(),
                customer.getCity(),
                customer.getPostalCode()
        );
        dto.setCustomerEventDto(customerDto);

        // === ProductItemEventDtos ===
        List<ProductItemEventDto> itemDtos = productItems.stream().map(item -> {
            Product product = productRepository.findProductByProductIdEvent(item.getProductIdEvent());

            ProductItemEventDto itemDto = new ProductItemEventDto();
            itemDto.setProductIdEvent(item.getProductIdEvent());
            itemDto.setQty(item.getQuantity());
            itemDto.setPrice(item.getPrice());
            itemDto.setDiscount(item.getDiscount());
            itemDto.setProductItemStatus("PENDING");

            if (product != null) {
                itemDto.setProductName(product.getName());
            } else {
                System.out.println("⚠️ Produit introuvable pour ID = " + item.getProductIdEvent());
            }

            return itemDto;
        }).toList();

        dto.setProductItemEventDtos(itemDtos);

        // === ProductEventDto (à partir du premier produit pour stock global par ex) ===
        List<ProductEventDto> productStockList = productItems.stream()
                .map(item -> {
                    Product product = productRepository.findProductByProductIdEvent(item.getProductIdEvent());
                    if (product == null) return null;

                    ProductEventDto pDto = new ProductEventDto();
                    pDto.setProductIdEvent(product.getProductIdEvent());
                    pDto.setQty(product.getQty());
                    pDto.setName(product.getName());

                    return pDto;
                })
                .filter(Objects::nonNull)
                .toList();

        dto.setProductEventDtos(productStockList); // 🔁 nouvelle liste dans DTO


        return dto;
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
                productRepository.updateProductQtyStatus(productModel.getProductIdEvent(), newStatus);
            }

        }
    }

  /*  public void sendOrderToCancel(String orderIdEvent,String username){
        // Order order=orderRepository.findByOrderIdEvent(orderIdEvent);

        OrderEventDto orderEventDto=new OrderEventDto();
        orderEventDto.setId(orderIdEvent);
        orderEventDto.setStatus(EventStatus.CANCELLING.name());
        orderEventDto.setUserName(username);
        orderProducer.sendMessage(orderEventDto);
    }*/

    public void sendOrderToCancel(String orderIdEvent, String username) {
        Order order = orderRepository.findByOrderIdEvent(orderIdEvent);
        if (order == null) {
            throw new RuntimeException("Commande non trouvée: " + orderIdEvent);
        }

        // Récupérer les produits de la commande
        List<ProductItem> productItems = productItemRepository.findByOrderOrderIdEvent(orderIdEvent);

        // Construire les ProductItemEventDto
        List<ProductItemEventDto> productItemDtos = productItems.stream().map(item -> {
            ProductItemEventDto dto = new ProductItemEventDto();
            dto.setProductIdEvent(item.getProductIdEvent());
            dto.setQty(item.getQuantity());
            dto.setPrice(item.getPrice());
            dto.setDiscount(item.getDiscount());
            dto.setProductItemStatus("CANCELLED"); // ou "PENDING"
            return dto;
        }).toList();

        // Récupérer les infos du client
        Customer customer = customerRepository.findCustomerByCustomerIdEvent(order.getCustomerIdEvent());
        CustomerEventDto customerDto = new CustomerEventDto(
                customer.getCustomerIdEvent(),
                customer.getName(),
                customer.getAddress(),
                customer.getPhone(),
                customer.getEmail(),
                customer.getAddress(),
                customer.getCity(),
                customer.getPostalCode()
        );

        // Créer l’OrderEventDto
        OrderEventDto orderEventDto = new OrderEventDto();
        orderEventDto.setId(order.getOrderIdEvent());
        orderEventDto.setStatus(EventStatus.CANCELLING.name());
        orderEventDto.setUserName(username);
        orderEventDto.setCustomerEventDto(customerDto);
        orderEventDto.setProductItemEventDtos(productItemDtos);

        // Envoyer le message Kafka
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
        // On récupère les ProductItems associés à des commandes avec ce statut
        List<ProductItem> productItems = productItemRepository.findByOrderOrderStatus(status);

        // On récupère tous les produits nécessaires
        List<Product> products = productRepository.findAll();
        Map<String, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getProductIdEvent, p -> p));

        // On enrichit chaque ProductItem avec son produit
        productItems.forEach(item -> {
            Product product = productMap.get(item.getProductIdEvent());
            if (product != null) {
                item.setProduct(product);
            }
        });

        return productItems;
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

    public List<OrderResponseDto> getAllOrdersWithDetails() {
        List<Order> orders = orderRepository.findAll();
        List<Customer> customers = customerRepository.findAll();
        List<ProductItem> productItems = productItemRepository.findAll();
        List<Product> products = productRepository.findAll();

        // Créer les maps
        Map<String, Customer> customerMap = customers.stream()
                .collect(Collectors.toMap(Customer::getCustomerIdEvent, c -> c));

        Map<String, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getProductIdEvent, p -> p));

        // Associer les clients
        orders.forEach(order -> order.setCustomer(customerMap.get(order.getCustomerIdEvent())));

        // Préparer la réponse
        List<OrderResponseDto> responseList = new ArrayList<>();

        for (Order order : orders) {
            OrderResponseDto dto = new OrderResponseDto();
            dto.setOrderId(order.getOrderIdEvent());

            Customer customer = order.getCustomer();
            if (customer != null) {
                dto.setCustomerName(customer.getName());
                dto.setCustomerEmail(customer.getEmail());
            }

            List<ProductItemResponseDto> itemDtos = productItems.stream()
                    .filter(item -> item.getOrder().getOrderIdEvent().equals(order.getOrderIdEvent()))
                    .map(item -> {
                        ProductItemResponseDto itemDto = new ProductItemResponseDto();
                        itemDto.setProductId(item.getProductIdEvent());

                        Product product = productMap.get(item.getProductIdEvent());
                        if (product != null) {
                            itemDto.setProductName(product.getName());
                        }

                        itemDto.setQuantity(item.getQuantity());
                        itemDto.setPrice(item.getPrice());
                        itemDto.setDiscount(item.getDiscount());
                        // 🧮 Calcul du montant net par produit et de la taxe (20%)
                        double netCal= (item.getPrice() * item.getQuantity()) - item.getDiscount();
                        double tax = Math.round(netCal * 0.20 * 100.0) / 100.0;
                        itemDto.setTax(tax);

                        return itemDto;
                    })
                    .collect(Collectors.toList());

            double subtotal = itemDtos.stream()
                    .mapToDouble(i -> i.getPrice() * i.getQuantity())
                    .sum();
            subtotal = Math.round(subtotal * 100.0) / 100.0;

            double totalDiscount = itemDtos.stream()
                    .mapToDouble(i -> {
                        double total = i.getPrice() * i.getQuantity();
                        if (total < 100) return 0;
                        else if (total < 200) return 0.005 * total;
                        else return 0.01 * total;
                    })
                    .sum();
            totalDiscount = Math.round(totalDiscount * 100.0) / 100.0;

            double totalTax = (subtotal - totalDiscount) * 0.20;
            totalTax = Math.round(totalTax * 100.0) / 100.0;

            double amount = (subtotal - totalDiscount) + totalTax;
            amount = Math.round(amount * 100.0) / 100.0;



            // Affecter les valeurs
            dto.setItems(itemDtos);
            dto.setTotalDiscount(totalDiscount);
            dto.setTotalTax(totalTax);
            dto.setAmount(amount);

            responseList.add(dto);
        }

        return responseList;
    }

    public List<OrderResponseDto> getOrdersByStatus(String status) {
        List<Order> orders = orderRepository.findByOrderStatus(status);
        List<Customer> customers = customerRepository.findAll();
        List<ProductItem> productItems = productItemRepository.findAll();
        List<Product> products = productRepository.findAll();

        // Maps
        Map<String, Customer> customerMap = customers.stream()
                .collect(Collectors.toMap(Customer::getCustomerIdEvent, c -> c));

        Map<String, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getProductIdEvent, p -> p));

        List<OrderResponseDto> result = new ArrayList<>();

        for (Order order : orders) {
            OrderResponseDto dto = new OrderResponseDto();
            dto.setOrderId(order.getOrderIdEvent());

            Customer customer = customerMap.get(order.getCustomerIdEvent());
            if (customer != null) {
                dto.setCustomerName(customer.getName());
                dto.setCustomerEmail(customer.getEmail());
            }

            List<ProductItemResponseDto> itemDtos = productItems.stream()
                    .filter(item -> item.getOrder().getOrderIdEvent().equals(order.getOrderIdEvent()))
                    .map(item -> {
                        ProductItemResponseDto itemDto = new ProductItemResponseDto();
                        itemDto.setProductId(item.getProductIdEvent());
                        itemDto.setQuantity(item.getQuantity());
                        itemDto.setPrice(item.getPrice());


                        Product product = productMap.get(item.getProductIdEvent());
                        if (product != null) {
                            itemDto.setProductName(product.getName());
                        }

                        return itemDto;
                    })
                    .collect(Collectors.toList());

            dto.setItems(itemDtos);
            result.add(dto);
        }

        return result;
    }



    public ProductItem getOrderById(Long id) {
        return productItemRepository.findById(id).orElse(null);
    }

    public List<OrderResponseDto> getOrdersWithDetailsByCustomer(String customerIdEvent) {
        List<Order> orders = orderRepository.findByCustomerIdEvent(customerIdEvent);

        if (orders.isEmpty()) return List.of(); // Aucun résultat ? Retourne une liste vide

        // Tu n’as besoin que du client courant
        Customer customer = customerRepository.findCustomerByCustomerIdEvent(customerIdEvent);
        if (customer == null) return List.of(); // Par sécurité

        // Tu récupères tous les productItems de ces commandes
        List<String> orderIds = orders.stream()
                .map(Order::getOrderIdEvent)
                .toList();
        List<ProductItem> productItems = productItemRepository.findByOrderOrderIdEventIn(orderIds);

        // Et les produits associés
        List<Product> products = productRepository.findAll(); // Tu peux filtrer pour optimiser
        Map<String, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getProductIdEvent, p -> p));

        // Construction des réponses
        List<OrderResponseDto> responseList = new ArrayList<>();

        for (Order order : orders) {
            OrderResponseDto dto = new OrderResponseDto();
            dto.setOrderId(order.getOrderIdEvent());
            dto.setCustomerName(customer.getName());
            dto.setCustomerEmail(customer.getEmail());

            List<ProductItemResponseDto> itemDtos = productItems.stream()
                    .filter(item -> item.getOrder().getOrderIdEvent().equals(order.getOrderIdEvent()))
                    .map(item -> {
                        ProductItemResponseDto itemDto = new ProductItemResponseDto();
                        itemDto.setProductId(item.getProductIdEvent());
                        itemDto.setQuantity(item.getQuantity());
                        itemDto.setPrice(item.getPrice());
                        itemDto.setDiscount(item.getDiscount());

                        Product product = productMap.get(item.getProductIdEvent());
                        if (product != null) {
                            itemDto.setProductName(product.getName());
                        }
                        return itemDto;
                    })
                    .collect(Collectors.toList());

            dto.setItems(itemDtos);
            responseList.add(dto);
        }

        return responseList;
    }

    public List<OrderResponseDto> getOrdersWithDetailsByCustomerAndStatus(String customerIdEvent, String status) {
        List<Order> orders = orderRepository.findByCustomerIdEventAndOrderStatus(customerIdEvent, status);

        if (orders.isEmpty()) return List.of();

        Customer customer = customerRepository.findCustomerByCustomerIdEvent(customerIdEvent);
        if (customer == null) return List.of();

        List<String> orderIds = orders.stream()
                .map(Order::getOrderIdEvent)
                .toList();

        List<ProductItem> productItems = productItemRepository.findByOrderOrderIdEventIn(orderIds);

        List<Product> products = productRepository.findAll();
        Map<String, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getProductIdEvent, p -> p));

        List<OrderResponseDto> responseList = new ArrayList<>();

        for (Order order : orders) {
            OrderResponseDto dto = new OrderResponseDto();
            dto.setOrderId(order.getOrderIdEvent());
            dto.setCustomerName(customer.getName());
            dto.setCustomerEmail(customer.getEmail());

            List<ProductItemResponseDto> itemDtos = productItems.stream()
                    .filter(item -> item.getOrder().getOrderIdEvent().equals(order.getOrderIdEvent()))
                    .map(item -> {
                        ProductItemResponseDto itemDto = new ProductItemResponseDto();
                        itemDto.setProductId(item.getProductIdEvent());
                        itemDto.setQuantity(item.getQuantity());
                        itemDto.setPrice(item.getPrice());
                        itemDto.setDiscount(item.getDiscount());

                        Product product = productMap.get(item.getProductIdEvent());
                        if (product != null) {
                            itemDto.setProductName(product.getName());
                        }
                        return itemDto;
                    })
                    .collect(Collectors.toList());

            dto.setItems(itemDtos);
            responseList.add(dto);
        }

        return responseList;
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

    public ProductItem findProductItemByOrderIdEventAndProductIdEvent(String orderIdEvent, String productIdEvent) {
        return  productItemRepository.findProductItemByOrderIdEventAndProductIdEvent( orderIdEvent, productIdEvent);
    }

    public OrderResponseDto getOrderWithDetailsById(String orderIdEvent) {
        Order order = orderRepository.findByOrderIdEvent(orderIdEvent);
        if (order == null) {
            throw new RuntimeException("Order not found: " + orderIdEvent);
        }

        Customer customer = customerRepository.findByCustomerIdEvent(order.getCustomerIdEvent()).orElse(null);
        List<ProductItem> productItems = productItemRepository.findByOrder_OrderIdEvent(orderIdEvent);
        List<Product> products = productRepository.findAll();

        Map<String, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getProductIdEvent, p -> p));

        OrderResponseDto dto = new OrderResponseDto();
        dto.setOrderId(order.getOrderIdEvent());

        if (customer != null) {
            dto.setCustomerName(customer.getName());
            dto.setCustomerEmail(customer.getEmail());
        }

        List<ProductItemResponseDto> itemDtos = productItems.stream()
                .map(item -> {
                    ProductItemResponseDto itemDto = new ProductItemResponseDto();
                    itemDto.setProductId(item.getProductIdEvent());
                    itemDto.setQuantity(item.getQuantity());
                    itemDto.setPrice(item.getPrice());
                    itemDto.setDiscount(item.getDiscount());
                    // 🧮 Calcul du montant net par produit et de la taxe (20%)
                    double netCal= (item.getPrice() * item.getQuantity()) - item.getDiscount();
                    double tax = Math.round(netCal * 0.20 * 100.0) / 100.0;
                    itemDto.setTax(tax);

                    Product product = productMap.get(item.getProductIdEvent());
                    if (product != null) {
                        itemDto.setProductName(product.getName());
                    }

                    return itemDto;
                }).collect(Collectors.toList());

        dto.setItems(itemDtos);

        // 🔢 Calcul total
        double subtotal = itemDtos.stream()
                .mapToDouble(i -> i.getPrice() * i.getQuantity())
                .sum();
        subtotal = Math.round(subtotal * 100.0) / 100.0;

        double totalDiscount = itemDtos.stream()
                .mapToDouble(i -> {
                    double total = i.getPrice() * i.getQuantity();
                    if (total < 100) return 0;
                    else if (total < 200) return 0.005 * total;
                    else return 0.01 * total;
                }).sum();
        totalDiscount = Math.round(totalDiscount * 100.0) / 100.0;

        double netAmount = subtotal - totalDiscount;
        netAmount = Math.round(netAmount * 100.0) / 100.0;

        double totalTax = netAmount * 0.2;
        totalTax = Math.round(totalTax * 100.0) / 100.0;

        double amount = netAmount + totalTax;
        amount = Math.round(amount * 100.0) / 100.0;

        dto.setAmount(amount);
        dto.setTotalDiscount(totalDiscount);
        dto.setTotalTax(totalTax);
        dto.setCreatedDate(order.getDate());


        return dto;
    }


    public List<OrderResponseDto> getOrdersWithDetailsByStatus(String status) {
        List<Order> orders = orderRepository.findAll()
                .stream()
                .filter(order -> order.getOrderStatus().equalsIgnoreCase(status))
                .collect(Collectors.toList());

        List<Customer> customers = customerRepository.findAll();
        List<ProductItem> productItems = productItemRepository.findAll();
        List<Product> products = productRepository.findAll();

        // Créer les maps
        Map<String, Customer> customerMap = customers.stream()
                .collect(Collectors.toMap(Customer::getCustomerIdEvent, c -> c));

        Map<String, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getProductIdEvent, p -> p));

        // Associer les clients
        orders.forEach(order -> order.setCustomer(customerMap.get(order.getCustomerIdEvent())));

        // Préparer la réponse
        List<OrderResponseDto> responseList = new ArrayList<>();

        for (Order order : orders) {
            OrderResponseDto dto = new OrderResponseDto();
            dto.setOrderId(order.getOrderIdEvent());
            dto.setCreatedDate(order.getDate());

            Customer customer = order.getCustomer();
            if (customer != null) {
                dto.setCustomerName(customer.getName());
                dto.setCustomerEmail(customer.getEmail());
            }

            List<ProductItemResponseDto> itemDtos = productItems.stream()
                    .filter(item -> item.getOrder().getOrderIdEvent().equals(order.getOrderIdEvent()))
                    .map(item -> {
                        ProductItemResponseDto itemDto = new ProductItemResponseDto();
                        itemDto.setProductId(item.getProductIdEvent());

                        Product product = productMap.get(item.getProductIdEvent());
                        if (product != null) {
                            itemDto.setProductName(product.getName());
                        }

                        itemDto.setQuantity(item.getQuantity());
                        itemDto.setPrice(item.getPrice());
                        itemDto.setDiscount(item.getDiscount());

                        double netCal = (item.getPrice() * item.getQuantity()) - item.getDiscount();
                        double tax = Math.round(netCal * 0.20 * 100.0) / 100.0;
                        itemDto.setTax(tax);

                        return itemDto;
                    })
                    .collect(Collectors.toList());

            double subtotal = itemDtos.stream()
                    .mapToDouble(i -> i.getPrice() * i.getQuantity())
                    .sum();
            subtotal = Math.round(subtotal * 100.0) / 100.0;

            double totalDiscount = itemDtos.stream()
                    .mapToDouble(i -> {
                        double total = i.getPrice() * i.getQuantity();
                        if (total < 100) return 0;
                        else if (total < 200) return 0.005 * total;
                        else return 0.01 * total;
                    })
                    .sum();
            totalDiscount = Math.round(totalDiscount * 100.0) / 100.0;

            double totalTax = (subtotal - totalDiscount) * 0.20;
            totalTax = Math.round(totalTax * 100.0) / 100.0;

            double amount = (subtotal - totalDiscount) + totalTax;
            amount = Math.round(amount * 100.0) / 100.0;

            // Affecter les valeurs
            dto.setItems(itemDtos);
            dto.setTotalDiscount(totalDiscount);
            dto.setTotalTax(totalTax);
            dto.setAmount(amount);

            responseList.add(dto);
        }

        return responseList;
    }


    public OrderResponseDto getOrderWithDetailsByStatusAndId(String status, String orderId) {
        Order order = orderRepository.findByOrderIdEvent(orderId);
        if (order == null || !order.getOrderStatus().equalsIgnoreCase(status)) {
            return null;
        }

        Customer customer = customerRepository.findByCustomerIdEvent(order.getCustomerIdEvent())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        List<ProductItem> productItems = productItemRepository.findByOrder_OrderIdEvent(orderId);
        List<Product> products = productRepository.findAll();

        Map<String, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getProductIdEvent, p -> p));

        OrderResponseDto dto = new OrderResponseDto();
        dto.setOrderId(order.getOrderIdEvent());

        if (customer != null) {
            dto.setCustomerName(customer.getName());
            dto.setCustomerEmail(customer.getEmail());
        }

        List<ProductItemResponseDto> itemDtos = productItems.stream()
                .map(item -> {
                    ProductItemResponseDto itemDto = new ProductItemResponseDto();
                    itemDto.setProductId(item.getProductIdEvent());

                    Product product = productMap.get(item.getProductIdEvent());
                    if (product != null) {
                        itemDto.setProductName(product.getName());
                    }

                    itemDto.setQuantity(item.getQuantity());
                    itemDto.setPrice(item.getPrice());
                    itemDto.setDiscount(item.getDiscount());

                    double net = (item.getPrice() * item.getQuantity()) - item.getDiscount();
                    double tax = Math.round(net * 0.20 * 100.0) / 100.0;
                    itemDto.setTax(tax);

                    return itemDto;
                })
                .collect(Collectors.toList());

        double subtotal = itemDtos.stream()
                .mapToDouble(i -> i.getPrice() * i.getQuantity())
                .sum();
        subtotal = Math.round(subtotal * 100.0) / 100.0;

        double totalDiscount = itemDtos.stream()
                .mapToDouble(i -> {
                    double total = i.getPrice() * i.getQuantity();
                    if (total < 100) return 0;
                    else if (total < 200) return 0.005 * total;
                    else return 0.01 * total;
                })
                .sum();
        totalDiscount = Math.round(totalDiscount * 100.0) / 100.0;

        double totalTax = (subtotal - totalDiscount) * 0.20;
        totalTax = Math.round(totalTax * 100.0) / 100.0;

        double amount = (subtotal - totalDiscount) + totalTax;
        amount = Math.round(amount * 100.0) / 100.0;

        dto.setItems(itemDtos);
        dto.setTotalDiscount(totalDiscount);
        dto.setTotalTax(totalTax);
        dto.setAmount(amount);

        return dto;
    }

}

