package com.gogo.delivered_command_service.service;

import com.gogo.base_domaine_service.event.CustomerEventDto;
import com.gogo.base_domaine_service.event.EventStatus;
import com.gogo.base_domaine_service.event.OrderEventDto;
import com.gogo.delivered_command_service.kafka.DeliveredCommandProducer;
import com.gogo.delivered_command_service.model.Delivered;
import com.gogo.delivered_command_service.repository.DeliveredCommandRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public void saveAndSendDeliveredCommand(Delivered delivered) {
       // List<Delivered> deliveredList=this.findByPaymentAndStatus(delivered.getPaymentId(),EventStatus.DELIVERING.name());
     // Vérifier si cette commande est déjà  délivrée
      //  boolean isAlreadyProcessed = this.isOrderAlreadyProcessed(delivered.getPaymentId());
       // for (Delivered orderDelivered:deliveredList){
        	//if (!isAlreadyProcessed) {
    	Delivered existingDelivered=deliveredCommandRepository.findByCustomerIdAndOrderIdAndStatus(delivered.getCustomerId(),delivered.getOrderId(), EventStatus.DELIVERING.name());
        		OrderEventDto orderEventDto=new OrderEventDto();
                CustomerEventDto customerEventDto=new CustomerEventDto();

                existingDelivered.setStatus(EventStatus.DELIVERED.name());
                existingDelivered.setDetails("Order is delivered");
                deliveredCommandRepository.save(existingDelivered);

                customerEventDto.setCustomerIdEvent(existingDelivered.getCustomerId());
                customerEventDto.setName(existingDelivered.getCustomerName());
                customerEventDto.setEmail(existingDelivered.getCustomerMail());

                orderEventDto.setStatus(EventStatus.DELIVERED.name());
                orderEventDto.setId(existingDelivered.getOrderId());
                orderEventDto.setPaymentId(existingDelivered.getPaymentId());
                orderEventDto.setCustomerEventDto(customerEventDto);

                deliveredCommandProducer.sendMessage(orderEventDto);

        	//}
            
        //}
    }
    
    public boolean isOrderAlreadyProcessed(String paymentId) {
        List<Delivered> events = deliveredCommandRepository.findByPaymentIdAndStatus(paymentId,EventStatus.DELIVERING.name());
        
        return !events.isEmpty(); // Retourne true si la commande est déjà traitée
    }
    public boolean isOrderAlreadyProcessed2(String paymentId,String orderId) {
        List<Delivered> events = deliveredCommandRepository.findByPaymentIdAndOrderIdAndStatus(paymentId,orderId,EventStatus.DELIVERING.name());
        
        return !events.isEmpty(); // Retourne true si la commande est déjà traitée
    }
    
}
