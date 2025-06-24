package com.gogo.delivered_query_service.service;


import com.gogo.delivered_query_service.dto.DeliveredResponseDto;
import com.gogo.delivered_query_service.mapper.DeliveredMapper;
import com.gogo.delivered_query_service.model.Bill;
import com.gogo.delivered_query_service.model.Delivered;
import com.gogo.delivered_query_service.repository.BillRepository;
import com.gogo.delivered_query_service.repository.DeliveredQueryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class DeliveredQueryService {
    @Autowired
    private DeliveredQueryRepository deliveredQueryRepository;
    @Autowired
    private BillRepository billRepository;

    public List<Delivered> getAllDelivers(){
        return deliveredQueryRepository.findAll();
    }
    public Delivered getDeliver(Long id){
        return deliveredQueryRepository.findById(id).orElse(null);
    }

    public void saveDeliveredQuery(Delivered delivered){
        deliveredQueryRepository.save(delivered);
    }




    public Optional<Delivered> findByOrderIdAndStatus(String orderId, String status) {
        return Optional.ofNullable(deliveredQueryRepository.findByOrderIdAndStatus(orderId, status));
    }

    public boolean existsByOrderIdAndStatus(String orderId, String status) {
        return deliveredQueryRepository.existsByOrderIdAndStatus( orderId, status);
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


    public List<DeliveredResponseDto> getAllShipsWithProducts() {
        List<Delivered> delivers = deliveredQueryRepository.findAll();
        List<DeliveredResponseDto> result = new ArrayList<>();

        for (Delivered delivered : delivers) {
            List<Bill> bills = billRepository.findByOrderRef(delivered.getOrderId());
            DeliveredResponseDto dto = DeliveredMapper.mapToDeliveredResponseDto(delivered, bills);
            result.add(dto);
        }

        return result;
    }



    public DeliveredResponseDto findDeliverWithDetails(String orderId) {
        Delivered delivered = deliveredQueryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Delivered not found for ID: " + orderId));

        List<Bill> bills = billRepository.findByOrderRef(delivered.getOrderId());

        return DeliveredMapper.mapToDeliveredResponseDto(delivered, bills);
    }
}
