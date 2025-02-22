package com.gogo.inventory_service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Value("${spring.kafka.topic.name}")
    private String topicName;


    // spring bean for kafka topic
    @Primary
    @Bean
    public NewTopic topic(){
        return TopicBuilder.name(topicName)
                .build();
    }

    @Value("${spring.kafka.topic.product.update.name}")
    private String topicName2;

    // spring bean for kafka topic
    @Bean
    public NewTopic topic2(){
        return TopicBuilder.name(topicName2)
                .build();
    }

    @Value("${spring.kafka.topic.quantity.update.name}")
    private String topicName3;

    // spring bean for kafka topic
    @Bean
    public NewTopic topic3(){
        return TopicBuilder.name(topicName3)
                .build();
    }


}

