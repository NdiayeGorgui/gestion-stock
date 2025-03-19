package com.gogo.delivered_query_service.service;


import com.gogo.base_domaine_service.event.EventStatus;
import com.gogo.delivered_query_service.model.Delivered;
import com.gogo.delivered_query_service.repository.DeliveredQueryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DeliveredQueryService {
    @Autowired
    private DeliveredQueryRepository deliveredQueryRepository;

    public List<Delivered> getAllDelivers(){
        return deliveredQueryRepository.findAll();
    }
    public Delivered getDeliver(Long id){
        return deliveredQueryRepository.findById(id).orElse(null);
    }

    public void saveDeliveredQuery(Delivered delivered){
        deliveredQueryRepository.save(delivered);
    }

    public Delivered findByOrder(String orderId){
        return deliveredQueryRepository.findByOrderId(orderId);
    }
	public List<Delivered> findByPaymentIdAndOrderIdAndStatus(String paymentId,String orderId, String status) {
		 return deliveredQueryRepository.findByPaymentIdAndOrderIdAndStatus(paymentId,orderId,status);
	}
	public boolean isOrderAlreadyProcessed(String paymentId,String orderId) {
		 List<Delivered> events = deliveredQueryRepository.findByPaymentIdAndOrderIdAndStatus(paymentId,orderId,EventStatus.DELIVERING.name());
	        
	        return !events.isEmpty(); // Retourne true si la commande est déjà traitée
	}
	public Delivered findByCustomerIdAndOrderIdAndStatus(String customerIdEvent,String OrderId, String status) {
		
		return deliveredQueryRepository.findByCustomerIdAndOrderIdAndStatus(customerIdEvent,OrderId, status);
	}
	public Delivered findByOrderId(String orderId) {
		
		return deliveredQueryRepository.findByOrderId( orderId);
	}

}
