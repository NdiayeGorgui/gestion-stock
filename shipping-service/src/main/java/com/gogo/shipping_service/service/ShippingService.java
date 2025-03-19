package com.gogo.shipping_service.service;

import com.gogo.base_domaine_service.event.CustomerEventDto;
import com.gogo.base_domaine_service.event.EventStatus;
import com.gogo.base_domaine_service.event.OrderEventDto;
import com.gogo.shipping_service.kafka.ShippingProducer;
import com.gogo.shipping_service.mapper.ShippingMapper;
import com.gogo.shipping_service.model.Ship;
import com.gogo.shipping_service.repository.ShippingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShippingService {
    @Autowired
    private ShippingRepository shippingRepository;
    @Autowired
    private ShippingProducer shippingProducer;

    public void saveShip(Ship ship){
        shippingRepository.save(ship);
    }

    public List<Ship> findAllShips() {
        return shippingRepository.findAll();
    }

    public List<Ship> findByPaymentId(String paymentId) {
       return shippingRepository.findByPaymentId(paymentId);
    }

	public List<Ship> findByCustomer(String customerId) {
		
		return shippingRepository.findByCustomerId( customerId);
	}

	public void saveAndSendShip(Ship ship) {
		// List<Ship> customerShips=this.findByCustomer(ship.getCustomerId());
		 Ship existingShip=shippingRepository.findByCustomerIdAndOrderIdAndStatus(ship.getCustomerId(),ship.getOrderId(), EventStatus.SHIPPING.name());
		  
			  OrderEventDto orderEventDto=new OrderEventDto();
			  CustomerEventDto customerEventDto=new CustomerEventDto();
			  existingShip.setDetails("Order is Shipped");
			  existingShip.setStatus(EventStatus.SHIPPED.name());
			  shippingRepository.save(existingShip);
			  orderEventDto.setId(existingShip.getOrderId());
			  orderEventDto.setPaymentId(existingShip.getPaymentId());
			  orderEventDto.setStatus(EventStatus.SHIPPED.name());
			  
			  customerEventDto.setCustomerIdEvent(existingShip.getCustomerId());
			  customerEventDto.setName(existingShip.getCustomerName());
			  customerEventDto.setEmail(existingShip.getCustomerMail());
			  
			  orderEventDto.setCustomerEventDto(customerEventDto);
			  
			  shippingProducer.sendMessage(orderEventDto);
			 
		  
		 // Ship savedShip=ShippingMapper.mapToShip(orderEventDto);
		  
		
	}

	public List<Ship> findByPaymentAndStatus(String paymentId,String orderId, String status) {
		 return shippingRepository.findByPaymentIdAndOrderIdAndStatus(paymentId,orderId,status);
	}

	public boolean isOrderAlreadyProcessed(String paymentId,String orderId) {
		 List<Ship> events = shippingRepository.findByPaymentIdAndOrderIdAndStatus(paymentId,orderId,EventStatus.SHIPPING.name());
	        
	        return !events.isEmpty(); // Retourne true si la commande est déjà traitée
	}

	public Ship findByOrderId(String orderId) {
		
		return shippingRepository.findByOrderId( orderId);
	}
}
