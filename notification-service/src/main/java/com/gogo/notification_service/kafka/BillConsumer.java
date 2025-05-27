package com.gogo.notification_service.kafka;

import com.gogo.base_domaine_service.event.OrderEventDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;

@Service
public class BillConsumer {

    private final JavaMailSender javaMailSender;
    private static final Logger LOGGER = LoggerFactory.getLogger(BillConsumer.class);


    public BillConsumer(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }
    @KafkaListener(
            topics = "${spring.kafka.topic.billing.name}"
            ,groupId = "${spring.kafka.consumer.group-id}"
    )
    public void billConsumer(OrderEventDto event) {
        if (event == null || event.getCustomerEventDto() == null) {
            LOGGER.warn("Received null event or customer details are missing.");
            return;
        }

        String status = event.getStatus().toUpperCase();
        String customerName = event.getCustomerEventDto().getName();
        String customerEmail = event.getCustomerEventDto().getEmail();
        String orderId = event.getId();

        switch (status) {
            case "CREATED" -> sendEmailNotification(
                    customerEmail,
                    orderId,
                    "Order Created Notification",
                    String.format("""
                            ==========================================================
                            Order Created Notification
                            ==========================================================
                            
                            Hi %s,
                            Your order with order number %s has been created successfully!.
                            
                            Best regards!
                            
                            Trocady Solution Inc Team.
                            """, customerName, orderId)
            );

            case "CANCELED" -> sendEmailNotification(
                    customerEmail,
                    orderId,
                    "Order Canceled Notification",
                    String.format("""
                            ==========================================================
                            Order Canceled Notification
                            ==========================================================
                            
                            Hi %s,
                            Your order with order number %s has been canceled successfully!.
                            
                            Best regards!
                            
                            Trocady Solution Inc Team.
                            """, customerName, orderId)
            );

            default -> LOGGER.warn("Unhandled order event status: {}", status);
        }

        LOGGER.info("Order event received in billing service => {}", event);
    }

    private void sendEmailNotification(String to, String orderId, String subject, String body) {
        MimeMessagePreparator messagePreparator = mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
            messageHelper.setFrom("noreply@trocady.com");
            messageHelper.setTo(to);
            messageHelper.setSubject(String.format("%s - Order # %s", subject, orderId));
            messageHelper.setText(body);
        };

        try {
            javaMailSender.send(messagePreparator);
            LOGGER.info("Order notification mail sent to {} for order {}", to, orderId);
        } catch (MailException ex) {
            LOGGER.error("Exception occurred when sending email to {} for order {}", to, orderId, ex);
            throw new RuntimeException("Exception occurred when sending email.", ex);
        }
    }
}
