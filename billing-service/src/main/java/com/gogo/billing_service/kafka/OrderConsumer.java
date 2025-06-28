package com.gogo.billing_service.kafka;

import com.gogo.base_domaine_service.event.EventStatus;
import com.gogo.base_domaine_service.event.OrderEventDto;
import com.gogo.base_domaine_service.event.ProductItemEventDto;
import com.gogo.billing_service.Repository.BillRepository;
import com.gogo.billing_service.mapper.BillMapper;
import com.gogo.billing_service.model.Bill;
import com.gogo.billing_service.service.BillingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderConsumer {

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private BillProducer billProducer;

    @Autowired
    private BillingService billingService;

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderConsumer.class);

    @KafkaListener(
            topics = "${spring.kafka.topic.name}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumeOrder(OrderEventDto event) {
        LOGGER.info("🧾 Order event received in billing service => {}", event);
        if (event.getProductItemEventDtos() != null) {
            event.getProductItemEventDtos().forEach(item ->
                    LOGGER.info("➡️ Produit reçu: id={}, nom={}, qty={}", item.getProductIdEvent(), item.getProductName(), item.getQty())
            );
        } else {
            LOGGER.warn("⚠️ Aucune liste de ProductItemEventDto reçue dans l'event !");
        }


        if (EventStatus.PENDING.name().equalsIgnoreCase(event.getStatus())) {
            // Map to list of bills (1 bill per product item)
            List<Bill> bills = BillMapper.mapToBills(event);
            billRepository.saveAll(bills);
            LOGGER.info("✅ Bills saved: {}", bills.size());

            if (!bills.isEmpty()) {
                event.setStatus(EventStatus.CREATED.name());
                billingService.updateTheBillStatus(event.getId(), event.getStatus());
                LOGGER.info("📬 Bill(s) created, event updated to CREATED => {}", event);
                billProducer.sendMessage(event);
            } else {
                event.setStatus(EventStatus.FAILED.name());
                LOGGER.warn("❌ Failed to create bills => {}", event);
                billProducer.sendMessage(event);
            }
        }

        // ✅ Annulation d'une commande complète (ORDER)
        else if (EventStatus.CANCELLING.name().equalsIgnoreCase(event.getStatus())) {
            List<Bill> bills = billingService.findAllByOrderRef(event.getId());

            if (bills != null && !bills.isEmpty()) {
                List<ProductItemEventDto> canceledItems = new java.util.ArrayList<>();
                int canceledCount = 0;

                for (Bill bill : bills) {
                    if (EventStatus.CREATED.name().equalsIgnoreCase(bill.getStatus())) {
                        bill.setStatus(EventStatus.CANCELED.name());
                        billRepository.save(bill);
                        canceledCount++;

                        // 🔁 Créer le ProductItemEventDto pour renvoyer les infos du produit annulé
                        ProductItemEventDto item = new ProductItemEventDto();
                        item.setProductIdEvent(bill.getProductIdEvent());
                        item.setProductName(bill.getProductName());
                        item.setQty(bill.getQuantity());
                        item.setPrice(bill.getPrice());
                        item.setDiscount(bill.getDiscount());
                        item.setProductItemStatus(EventStatus.CANCELED.name());

                        canceledItems.add(item);

                        LOGGER.info("🛑 Canceled product: {} from order: {}", bill.getProductIdEvent(), bill.getOrderRef());
                    }
                }

                if (canceledCount > 0) {
                    event.setProductItemEventDtos(canceledItems); // ✅ on envoie les infos de tous les produits
                    event.setStatus(EventStatus.CANCELED.name());
                    billProducer.sendMessage(event);
                    LOGGER.info("🚫 Order '{}' canceled with {} item(s). Event sent.", event.getId(), canceledCount);
                } else {
                    LOGGER.warn("⚠️ No eligible bills to cancel for order: {}", event.getId());
                }
            } else {
                LOGGER.warn("⚠️ No bills found for order: {}", event.getId());
            }
        }

    }
}
