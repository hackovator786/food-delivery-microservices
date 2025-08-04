package com.hackovation.menu_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackovation.menu_service.dto.MenuItemEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class MenuItemEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    
    public void publishMenuItemCreated(MenuItemEvent event) {
        publishEvent("menu-item-created", event);
    }
    
    public void publishMenuItemUpdated(MenuItemEvent event) {
        publishEvent("menu-item-updated", event);
    }
    
    public void publishMenuItemDeleted(String menuItemId) {
        MenuItemEvent event = MenuItemEvent.builder()
                .id(menuItemId)
                .eventType("DELETED")
                .build();
        publishEvent("menu-item-deleted", event);
    }
    
    private void publishEvent(String topic, MenuItemEvent event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, message);
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Sent message=[{}] with offset=[{}]", 
                            message, 
                            result.getRecordMetadata().offset());
                } else {
                    log.error("Unable to send message=[{}] due to: {}", 
                            message, 
                            ex.getMessage());
                }
            });
        } catch (JsonProcessingException e) {
            log.error("Error serializing menu item event: {}", e.getMessage(), e);
        }
    }
}
