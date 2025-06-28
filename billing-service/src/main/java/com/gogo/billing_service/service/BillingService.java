package com.gogo.billing_service.service;


import com.gogo.billing_service.Repository.BillRepository;

import com.gogo.billing_service.dto.BillResponseDto;
import com.gogo.billing_service.dto.ProductItemDto;
import com.gogo.billing_service.model.Bill;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BillingService {

    @Autowired
    private BillRepository billRepository;
    public Bill getBill(Long id){
        return billRepository.findById(id).orElse(null);
    }
    public void updateBillStatus(String productIdEvent, String status){
        billRepository.updateBillStatus(productIdEvent, status);
    }

    public void updateTheBillStatus(String orderIdEvent, String status){
        billRepository.updateTheBillStatus(orderIdEvent, status);
    }

    public void saveBill(Bill bill){
        billRepository.save(bill);
    }

    public void updateAllBillCustomerStatus(String customerIdEvent,String status){
        billRepository.updateAllBillCustomerStatus(customerIdEvent, status);
    }

   /* public  Bill  findByOrderRef(String orderRef){
        return billRepository.findByOrderRef(orderRef);
    }*/

    public List<Bill> billList(String customerIdEvent,String status){
        return billRepository.findByCustomerIdEventAndStatus(customerIdEvent,status);
    }

    public List<Bill> getBills(String customerIdEvent){
        List<Bill> customerBills=billRepository.findAll();
        return customerBills.stream()
                .filter(bill -> bill.getCustomerIdEvent().equalsIgnoreCase(customerIdEvent))
                .collect(Collectors.toList());
    }

    public  double getAmount(String customerIdEvent){
        return billRepository.sumBill(customerIdEvent);
    }

    public  List<Bill> getBills(){
        return billRepository.findAll();
    }

    /*public Bill findByOrderIdEvent(String orderIdEvent){
        return billRepository.findByOrderRef(orderIdEvent);
    }*/
    public Bill findFirstByOrderIdEvent(String orderIdEvent) {
        return billRepository.findAllByOrderRef(orderIdEvent)
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Aucune facture trouvée pour la commande : " + orderIdEvent));
    }


    public List<Bill> findByOrderRefAndProductIdEvent(String orderRef, String productIdEvent) {
        return  billRepository.findByOrderRefAndProductIdEvent(orderRef,productIdEvent);
    }

    public List<BillResponseDto> getAllBillsWithProducts() {
        List<Bill> allBills = billRepository.findAll();

        // Regrouper les bills par orderRef
        Map<String, List<Bill>> groupedByOrder = allBills.stream()
                .collect(Collectors.groupingBy(Bill::getOrderRef));

        List<BillResponseDto> responseList = new ArrayList<>();

        for (Map.Entry<String, List<Bill>> entry : groupedByOrder.entrySet()) {
            String orderId = entry.getKey();
            List<Bill> billsForOrder = entry.getValue();

            Bill first = billsForOrder.get(0); // Infos client

            // ✅ Subtotal brut
            double subtotal = billsForOrder.stream()
                    .mapToDouble(b -> b.getPrice() * b.getQuantity())
                    .sum();
            subtotal = Math.round(subtotal * 100.0) / 100.0;

            // ✅ Total des remises
            double totalDiscount = billsForOrder.stream()
                    .mapToDouble(Bill::getDiscount)
                    .sum();
            totalDiscount = Math.round(totalDiscount * 100.0) / 100.0;

            // ✅ Construction des produits avec taxe unitaire
            List<ProductItemDto> productItems = billsForOrder.stream().map(b -> {
                ProductItemDto dto = new ProductItemDto();
                dto.setProductId(b.getProductIdEvent());
                dto.setProductName(b.getProductName());
                dto.setQuantity(b.getQuantity());
                dto.setPrice(b.getPrice());
                dto.setDiscount(b.getDiscount());

                double net = (b.getPrice() * b.getQuantity()) - b.getDiscount();
                double tax = Math.round(net * 0.20 * 100.0) / 100.0;
                dto.setTax(tax);

                return dto;
            }).toList();

            // ✅ Recalcul du totalTax basé sur la somme des taxes individuelles
            double totalTax = productItems.stream()
                    .mapToDouble(ProductItemDto::getTax)
                    .sum();
            totalTax = Math.round(totalTax * 100.0) / 100.0;

            // ✅ Montant total TTC
            double amount = subtotal - totalDiscount + totalTax;
            amount = Math.round(amount * 100.0) / 100.0;

            // ✅ Création du DTO
            BillResponseDto dto = new BillResponseDto();
            dto.setOrderId(orderId);
            dto.setCustomerName(first.getCustomerName());
            dto.setCustomerPhone(first.getCustomerPhone());
            dto.setCustomerMail(first.getCustomerMail());
            dto.setAmount(amount);
            dto.setTotalTax(totalTax);
            dto.setTotalDiscount(totalDiscount);
            dto.setBillStatus(first.getStatus());
            dto.setBillingDate(first.getBillingDate());
            dto.setProducts(productItems);

            responseList.add(dto);
        }

        return responseList;
    }


    public BillResponseDto findBillWithDetails(String orderRef) {
        List<Bill> bills = billRepository.findAllByOrderRef(orderRef);

        if (bills.isEmpty()) {
            throw new RuntimeException("Aucune facture trouvée pour l'orderRef : " + orderRef);
        }

        Bill first = bills.get(0); // Pour les infos client

        double subtotal = bills.stream()
                .mapToDouble(b -> b.getPrice() * b.getQuantity())
                .sum();
        subtotal = Math.round(subtotal * 100.0) / 100.0;

        double totalDiscount = bills.stream()
                .mapToDouble(Bill::getDiscount)
                .sum();
        totalDiscount = Math.round(totalDiscount * 100.0) / 100.0;

        double net = subtotal - totalDiscount;
        net = Math.round(net * 100.0) / 100.0;

        double totalTax = net * 0.2;
        totalTax = Math.round(totalTax * 100.0) / 100.0;

        double amount = net + totalTax;
        amount = Math.round(amount * 100.0) / 100.0;

        List<ProductItemDto> products = bills.stream().map(b -> {
            ProductItemDto dto = new ProductItemDto();
            dto.setProductId(b.getProductIdEvent());
            dto.setProductName(b.getProductName());
            dto.setQuantity(b.getQuantity());
            dto.setPrice(b.getPrice());
            dto.setDiscount(b.getDiscount());

            // 🧮 Calcul du montant net par produit et de la taxe (20%)
            double netCal= (b.getPrice() * b.getQuantity()) - b.getDiscount();
            double tax = Math.round(netCal * 0.20 * 100.0) / 100.0;
            dto.setTax(tax);

            return dto;
        }).toList();


        return new BillResponseDto(
                orderRef,
                first.getCustomerName(),
                first.getCustomerPhone(),
                first.getCustomerMail(),
                amount,
                totalTax,
                totalDiscount,
                first.getStatus(),
                first.getBillingDate(),
                products
        );
    }

    public List<Bill> getBillsByOrderRefAndStatus(String orderRef, String status) {
        return billRepository.findAllByOrderRef(orderRef).stream()
                .filter(bill -> bill.getStatus().equalsIgnoreCase(status))
                .toList();
    }

    public List<Bill> findAllByOrderRef(String orderRef) {
        return billRepository.findAllByOrderRef(orderRef);
    }



}
