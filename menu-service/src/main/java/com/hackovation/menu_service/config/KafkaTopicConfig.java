package com.hackovation.menu_service.config;

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
    public NewTopic menuItemCreatedTopic() {
        return new NewTopic("menu-item-created", 1, (short) 1);
    }
}
