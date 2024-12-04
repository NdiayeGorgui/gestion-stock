package com.gogo.order_service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Value("${spring.kafka.topic.order.name}")
    private String topicOrderName;

    @Primary
    // spring bean for kafka topic
    @Bean
    public NewTopic topicOrder(){
        return TopicBuilder.name(topicOrderName)
                .build();
    }

    @Value("${spring.kafka.topic.customer.name}")
    private String topicCustomerName;


    // spring bean for kafka topic
    @Bean
    public NewTopic topicCustomer(){
        return TopicBuilder.name(topicCustomerName)
                .build();
    }
    @Value("${spring.kafka.topic.product.name}")
    private String topicProductName;


    // spring bean for kafka topic
    @Bean
    public NewTopic topicProduct(){
        return TopicBuilder.name(topicProductName)
                .build();
    }
    @Value("${spring.kafka.topic.customer.update.name}")
    private String topicCustomerUpdateName;


    // spring bean for kafka topic
    @Bean
    public NewTopic topicCustomerUpdate(){
        return TopicBuilder.name(topicCustomerUpdateName)
                .build();
    }

    @Value("${spring.kafka.topic.product.update.name}")
    private String topicProductUpdateName;


    // spring bean for kafka topic
    @Bean
    public NewTopic topicProductUpdate(){
        return TopicBuilder.name(topicProductUpdateName)
                .build();
    }

}

