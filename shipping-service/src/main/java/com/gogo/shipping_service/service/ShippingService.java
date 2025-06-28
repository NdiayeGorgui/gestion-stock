package com.gogo.shipping_service.service;

import com.gogo.base_domaine_service.event.CustomerEventDto;
import com.gogo.base_domaine_service.event.EventStatus;
import com.gogo.base_domaine_service.event.OrderEventDto;
import com.gogo.shipping_service.dto.ShipResponseDto;
import com.gogo.shipping_service.kafka.ShippingProducer;
import com.gogo.shipping_service.mapper.ShippingMapper;
import com.gogo.shipping_service.model.Bill;
import com.gogo.shipping_service.model.Ship;
import com.gogo.shipping_service.repository.BillRepository;
import com.gogo.shipping_service.repository.ShippingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ShippingService {
	@Autowired
	private ShippingRepository shippingRepository;
	@Autowired
	private ShippingProducer shippingProducer;
	@Autowired
	BillRepository billRepository;

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

	public void saveAndSendShip(Ship ship, String username) {
		// 🔍 Vérifier si déjà SHIPPED
		boolean alreadyShipped = shippingRepository.existsByOrderIdAndStatus(
				ship.getOrderId(),
				EventStatus.SHIPPED.name()
		);

		if (alreadyShipped) {
			throw new IllegalStateException("⚠️ Order already shipped: " + ship.getOrderId());
		}

		// 🔍 Vérifier s’il y a une commande en SHIPPING (en cours de traitement)
		Ship existingShip = shippingRepository.findByOrderIdAndStatus(
				ship.getOrderId(),
				EventStatus.SHIPPING.name()
		);

		if (existingShip == null) {
			throw new IllegalStateException("❌ No shipping record found in SHIPPING state for order: " + ship.getOrderId());
		}

		// ✅ Construire l’événement à envoyer
		OrderEventDto orderEventDto = new OrderEventDto();
		orderEventDto.setUserName(username);
		orderEventDto.setId(existingShip.getOrderId());
		orderEventDto.setPaymentId(existingShip.getPaymentId());
		orderEventDto.setStatus(EventStatus.SHIPPED.name());

		CustomerEventDto customerEventDto = new CustomerEventDto();
		customerEventDto.setCustomerIdEvent(existingShip.getCustomerId());
		customerEventDto.setName(existingShip.getCustomerName());
		customerEventDto.setEmail(existingShip.getCustomerMail());

		orderEventDto.setCustomerEventDto(customerEventDto);

		// 🚚 Envoi à Kafka
		shippingProducer.sendMessage(orderEventDto);

		// ✅ Mise à jour en base
		existingShip.setDetails("Order is shipped to customer");
		existingShip.setStatus(EventStatus.SHIPPED.name());
		existingShip.setEventTimeStamp(LocalDateTime.now());
		shippingRepository.save(existingShip);
	}



	public List<Ship> findByPaymentAndStatus(String paymentId,String orderId, String status) {
		return shippingRepository.findByPaymentIdAndOrderIdAndStatus(paymentId,orderId,status);
	}

	public boolean isOrderAlreadyProcessed(String paymentId,String orderId) {
		List<Ship> events = shippingRepository.findByPaymentIdAndOrderIdAndStatus(paymentId,orderId,EventStatus.SHIPPING.name());

		return !events.isEmpty(); // Retourne true si la commande est déjà traitée
	}

	public Ship findByOrderId(String orderId) {

		return shippingRepository.findByOrderId( orderId)
				.orElseThrow(() -> new RuntimeException("Ship not found for ID: " + orderId));
	}


	public Ship findByOrderIdAndStatus(String orderId, String status) {
		return shippingRepository.findByOrderIdAndStatus(orderId, status);
	}

	public List<ShipResponseDto> getAllShipsWithProducts() {
		List<Ship> ships = shippingRepository.findAll();
		List<ShipResponseDto> result = new ArrayList<>();

		for (Ship ship : ships) {
			List<Bill> bills = billRepository.findByOrderRef(ship.getOrderId());
			ShipResponseDto dto = ShippingMapper.mapToShipResponseDto(ship, bills);
			result.add(dto);
		}

		return result;
	}

	public void saveBill(Bill bill){
		billRepository.save(bill);
	}

	public Bill findByOrderIdAndProductIdEvent(String orderRef, String productIdEvent) {
		return billRepository.findByOrderRefAndProductIdEvent(orderRef,productIdEvent);
	}

	public void updateTheBillStatus(String orderIdEvent, String status){
		billRepository.updateTheBillStatus(orderIdEvent, status);
	}

	public ShipResponseDto findShipWithDetails(String orderId) {
		Ship ship = shippingRepository.findByOrderId(orderId)
				.orElseThrow(() -> new RuntimeException("Ship not found for ID: " + orderId));

		List<Bill> bills = billRepository.findByOrderRef(ship.getOrderId());

		return ShippingMapper.mapToShipResponseDto(ship, bills);
	}

}
