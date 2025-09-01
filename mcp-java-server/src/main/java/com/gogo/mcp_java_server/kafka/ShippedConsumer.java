package com.gogo.mcp_java_server.kafka;

import com.gogo.base_domaine_service.event.EventStatus;
import com.gogo.base_domaine_service.event.OrderEventDto;
import com.gogo.mcp_java_server.model.Delivered;
import com.gogo.mcp_java_server.model.Ship;
import com.gogo.mcp_java_server.repository.DeliveredRepository;
import com.gogo.mcp_java_server.repository.ShipRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class ShippedConsumer {

    @Autowired
    private ShipRepository shipRepository;


    private static final Logger LOGGER = LoggerFactory.getLogger(ShippedConsumer.class);

    @KafkaListener(
            topics = "${spring.kafka.topic.shipping.name}"
            , groupId = "${spring.kafka.consumer.ship.group-id}"
    )
    public void consumeShip(OrderEventDto event) {
        LOGGER.info("⚙️ Received Kafka event => {}", event);
        //save the delivered table with delivered status
        if (EventStatus.SHIPPED.name().equalsIgnoreCase(event.getStatus())) {

            Ship ship=new Ship();
            ship.setOrderId(event.getId());
            //ship.setPaymentId(event.getPaymentId());
            ship.setCustomerId(event.getCustomerEventDto().getCustomerIdEvent());
            ship.setCustomerName(event.getCustomerEventDto().getName());
            ship.setCustomerMail(event.getCustomerEventDto().getEmail());
            ship.setDetails("Order Shipped");
            ship.setStatus(EventStatus.SHIPPED.name());
            LOGGER.info("🚚 Ship pas encore enregistré : {}", ship);
            shipRepository.save(ship);
            LOGGER.info("🚚 Ship enregistré : {}", ship);
            LOGGER.info("Oder event received in mpc-server  with shipped status => {}", event);

        }
    }
}
