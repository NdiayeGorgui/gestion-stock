package com.gogo.payment_service.kafka;

import com.gogo.base_domaine_service.event.OrderEventDto;
import org.apache.kafka.clients.admin.NewTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
public class PaymentProducer {
    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentProducer.class);

    private final NewTopic topic;

    private final KafkaTemplate<String, OrderEventDto> kafkaTemplate;

    public PaymentProducer(NewTopic topic, KafkaTemplate<String, OrderEventDto> kafkaTemplate) {
        this.topic = topic;
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(OrderEventDto event){

        LOGGER.info(" Payment event => {}", event.toString());

        // create Message
        Message<OrderEventDto> message = MessageBuilder
                .withPayload(event)
                .setHeader(KafkaHeaders.TOPIC, topic.name())
                .build();
        kafkaTemplate.send(message);
    }
}
