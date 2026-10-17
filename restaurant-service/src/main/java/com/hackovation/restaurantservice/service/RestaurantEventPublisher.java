package com.hackovation.restaurantservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackovation.restaurantservice.dto.RestaurantEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class RestaurantEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void publishRestaurantCreated(RestaurantEvent event) {
        publishEvent("restaurant-created", event);
    }

    private void publishEvent(String topic, RestaurantEvent event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, message);
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Published restaurant event to topic [{}] with offset [{}]",
                            topic, result.getRecordMetadata().offset());
                } else {
                    log.error("Failed to publish restaurant event to topic [{}]: {}", topic, ex.getMessage());
                }
            });
        } catch (JsonProcessingException e) {
            log.error("Error serializing restaurant event: {}", e.getMessage(), e);
        }
    }
}
