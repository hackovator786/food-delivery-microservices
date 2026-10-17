package com.hackovation.search_service.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackovation.search_service.dto.RestaurantEvent;
import com.hackovation.search_service.model.RestaurantDocument;
import com.hackovation.search_service.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RestaurantEventListener {

    private final RestaurantRepository restaurantRepository;

    @KafkaListener(
        topics = "restaurant-created",
        groupId = "${spring.kafka.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleRestaurantCreated(String eventStr) {
        RestaurantEvent event = null;
        try {
            event = new ObjectMapper().readValue(eventStr, RestaurantEvent.class);
            log.info("Received restaurant event: {}", event);

            RestaurantDocument doc = restaurantRepository.findByRestaurantId(event.getRestaurantId());
            if (doc == null) {
                doc = new RestaurantDocument();
            }

            doc.setRestaurantId(event.getRestaurantId());
            doc.setRestaurantName(event.getName());
            doc.setDescription(event.getDescription());
            doc.setCuisine(event.getCuisine());
            doc.setCity(event.getCity());
            doc.setImageUrl(event.getImageUrl());
            doc.setRating(event.getAvgRating());

            restaurantRepository.save(doc);
            log.info("Successfully indexed restaurant: {}", event.getRestaurantId());

        } catch (Exception e) {
            log.error("Error processing restaurant event: {}", event, e);
        }
    }
}
