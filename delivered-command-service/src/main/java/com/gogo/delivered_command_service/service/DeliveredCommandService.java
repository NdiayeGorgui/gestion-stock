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

    public void saveAndSendDeliveredCommand(String orderId) throws DeliveredCommandNotFoundException {

        // Cherche la commande en DELIVERING
        Delivered existingDelivered = deliveredCommandRepository
                .findByOrderIdAndStatus(orderId, EventStatus.DELIVERING.name())
                .orElseThrow(() -> new DeliveredCommandNotFoundException("Order not in DELIVERING state: " + orderId));

        // Mise à jour de l’état
        existingDelivered.setStatus(EventStatus.DELIVERED.name());
        existingDelivered.setDetails("Order is delivered");
        existingDelivered.setEventTimeStamp(LocalDateTime.now());
        deliveredCommandRepository.save(existingDelivered);

        // Construction de l’événement à envoyer
        OrderEventDto event = new OrderEventDto();
        event.setId(existingDelivered.getOrderId());
        event.setStatus(EventStatus.DELIVERED.name());

        CustomerEventDto customer = new CustomerEventDto();
        customer.setCustomerIdEvent(existingDelivered.getCustomerId());
        customer.setName(existingDelivered.getCustomerName());
        customer.setEmail(existingDelivered.getCustomerMail());

        event.setCustomerEventDto(customer);

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
}
