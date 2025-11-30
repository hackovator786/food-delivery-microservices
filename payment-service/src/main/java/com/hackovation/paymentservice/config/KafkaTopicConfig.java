package com.hackovation.paymentservice.config;

import lombok.Getter;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicConfig {

    @Getter
    @Value("${spring.kafka.group-id}")
    private String groupId;

    @Bean
    public NewTopic orderRestaurantNotificationTopic() {
        return new NewTopic("order-payment-notification-topic", 1, (short) 1);
    }
}
