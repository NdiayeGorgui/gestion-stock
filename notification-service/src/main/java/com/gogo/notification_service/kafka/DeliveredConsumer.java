package com.gogo.notification_service.kafka;

import com.gogo.base_domaine_service.event.EventStatus;
import com.gogo.base_domaine_service.event.OrderEventDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;

@Service
public class DeliveredConsumer {

    @Autowired
    private JavaMailSender javaMailSender;


    private static final Logger LOGGER = LoggerFactory.getLogger(DeliveredConsumer.class);

    @KafkaListener(
            topics = "${spring.kafka.topic.delivered.name}"
            ,groupId = "${spring.kafka.consumer.group-id}"
    )
    public void deliverConsumer(OrderEventDto event){
        if(event.getStatus().equalsIgnoreCase(EventStatus.DELIVERED.name())){
            MimeMessagePreparator messagePreparator=mimeMessage -> {
                MimeMessageHelper messageHelper=new MimeMessageHelper(mimeMessage);
                messageHelper.setFrom("noreply@gorgui.com");
                messageHelper.setTo(event.getCustomerEventDto().getEmail());
                messageHelper.setSubject(String.format("Order delivered Notification with number %s",event.getId()));
                messageHelper.setText(String.format("""
                     ==========================================================
                     Order Delivered notification
                     ==========================================================
                     
                     Hi %s,
                     your order with order number %s has been delivered successfully !.
                     Best regards !
                     
                     Gorgui Solution Inc Team.
                       \s""",
                        event.getCustomerEventDto().getName() ,event.getId()));
            };

            try{
                javaMailSender.send(messagePreparator);
                LOGGER.info("Order notification mail sent => {}", event);
            }
            catch (MailException ex){
                LOGGER.error("Exception occurred when sending email.", ex);
                throw new RuntimeException("Exception occurred when sending email.", ex);
            }

            LOGGER.info("Order event received in notification service => {}", event);
        }
    }
}
