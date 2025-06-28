package com.gogo.delivered_command_service.service;

import com.gogo.base_domaine_service.event.CustomerEventDto;
import com.gogo.base_domaine_service.event.EventStatus;
import com.gogo.base_domaine_service.event.OrderEventDto;
import com.gogo.delivered_command_service.exception.DeliveredCommandNotFoundException;
import com.gogo.delivered_command_service.kafka.DeliveredCommandProducer;
import com.gogo.delivered_command_service.model.Delivered;
import com.gogo.delivered_command_service.repository.DeliveredCommandRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DeliveredCommandService {
    @Autowired
    private DeliveredCommandRepository deliveredCommandRepository;
    
  
    @Autowired
    private DeliveredCommandProducer deliveredCommandProducer;

    public void saveDeliveredCommand(Delivered delivered){
        deliveredCommandRepository.save(delivered);
    }
  

    public List<Delivered> findByPayment(String paymentId) {
        return deliveredCommandRepository.findByPaymentId(paymentId);
    }
    
    public List<Delivered> findByPaymentAndStatus(String paymentId, String status) {
        return deliveredCommandRepository.findByPaymentIdAndStatus(paymentId,status);
    }
    
    public List<Delivered> findByPaymentAndStatus2(String paymentId,String orderId, String status) {
        return deliveredCommandRepository.findByPaymentIdAndOrderIdAndStatus(paymentId,orderId,status);
    }

    public void saveAndSendDeliveredCommand(Delivered delivered, String username) {
        // 🔍 Vérifie si déjà DELIVERED
        boolean alreadyDelivered = deliveredCommandRepository.existsByOrderIdAndStatus(
                delivered.getOrderId(),
                EventStatus.DELIVERED.name()
        );

        if (alreadyDelivered) {
            throw new IllegalStateException("⚠️ Order already delivered: " + delivered.getOrderId());
        }

        // 🔍 Vérifie s’il existe une entrée en DELIVERING
        Delivered existingDelivered = deliveredCommandRepository
                .findByOrderIdAndStatus(delivered.getOrderId(), EventStatus.DELIVERING.name())
                .orElseThrow(() -> new IllegalStateException("❌ No delivery record found in DELIVERING state for order: " + delivered.getOrderId()));

        // 📝 Mise à jour
        existingDelivered.setStatus(EventStatus.DELIVERED.name());
        existingDelivered.setDetails("Order is delivered");
        existingDelivered.setEventTimeStamp(LocalDateTime.now());
        deliveredCommandRepository.save(existingDelivered);

        // 📦 Préparer l’événement
        OrderEventDto event = new OrderEventDto();
        event.setUserName(username);
        event.setId(existingDelivered.getOrderId());
        event.setStatus(EventStatus.DELIVERED.name());

        CustomerEventDto customer = new CustomerEventDto();
        customer.setCustomerIdEvent(existingDelivered.getCustomerId());
        customer.setName(existingDelivered.getCustomerName());
        customer.setEmail(existingDelivered.getCustomerMail());

        event.setCustomerEventDto(customer);

        // 🚀 Envoyer à Kafka
        deliveredCommandProducer.sendMessage(event);
    }



    public boolean isOrderAlreadyProcessed(String paymentId) {
        List<Delivered> events = deliveredCommandRepository.findByPaymentIdAndStatus(paymentId,EventStatus.DELIVERING.name());
        
        return !events.isEmpty(); // Retourne true si la commande est déjà traitée
    }
    public boolean isOrderAlreadyProcessed2(String paymentId,String orderId) {
        List<Delivered> events = deliveredCommandRepository.findByPaymentIdAndOrderIdAndStatus(paymentId,orderId,EventStatus.DELIVERING.name());
        
        return !events.isEmpty(); // Retourne true si la commande est déjà traitée
    }

    public boolean existsByOrderIdAndStatus(String orderId, String status) {
        return  deliveredCommandRepository.existsByOrderIdAndStatus(orderId, status);
    }

    public Delivered findByOrderIdAndStatus(String orderId, String status) {
        return deliveredCommandRepository.findByOrderIdAndStatus(orderId, status).orElse(null);
    }
}
