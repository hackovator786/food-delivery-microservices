package com.hackovation.authservice.config;

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
    public NewTopic userRegistrationTopic() {
        return new NewTopic("user-verification", 1, (short) 1);
    }

    @Bean
    public NewTopic loginAttemptTopic() {
        return new NewTopic("login-attempt", 1, (short) 1);
    }

}
