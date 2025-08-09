package com.hackovation.search_service.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackovation.search_service.dto.MenuItemEvent;
import com.hackovation.search_service.model.MenuItemDocument;
import com.hackovation.search_service.repository.MenuItemSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class MenuItemEventListener {

    @Autowired
    private MenuItemSearchRepository menuItemSearchRepository;

    @KafkaListener(
        topics = {"menu-item-created", "menu-item-updated"}, 
        groupId = "${spring.kafka.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleMenuItemCreatedOrUpdated(String eventStr) {
        MenuItemEvent event = null;
        try {
            event = new ObjectMapper().readValue(eventStr, MenuItemEvent.class);
            log.info("Received menu item event: {}", event);
            
            // Convert tags to document format
            Set<MenuItemDocument.TagDoc> tagDocs = event.getTags().stream()
                .map(MenuItemDocument.TagDoc::new)
                .collect(Collectors.toSet());

            // Create or update document
            MenuItemDocument doc = menuItemSearchRepository.findById(event.getMenuItemId())
                .orElse(new MenuItemDocument());
            
            doc.setMenuItemId(event.getMenuItemId());
            doc.setRestaurantId(event.getRestaurantId());
            doc.setRestaurantName(event.getRestaurantName());
            doc.setMenuItemName(event.getMenuItemName());
            doc.setDescription(event.getDescription());
            doc.setCategory(event.getCategory());
            doc.setPrice(event.getPrice());
            doc.setIsAvailable(event.getIsAvailable());
            doc.setTags(tagDocs);
            doc.setImageUrl(event.getImageUrl());
            doc.setUpdatedAt(Instant.now());
            
            if (doc.getCreatedAt() == null) {
                doc.setCreatedAt(Instant.now());
            }

            // Save to Elasticsearch
            menuItemSearchRepository.save(doc);
            log.info("Successfully processed menu item {}: {}", event.getEventType(), event.getMenuItemId());
            
        } catch (Exception e) {
            log.error("Error processing menu item event: {}", event, e);
        }
    }

    @KafkaListener(
        topics = "menu-item-deleted",
        groupId = "${spring.kafka.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleMenuItemDeleted(String eventStr) {
        MenuItemEvent event = null;
        try {
            event = new ObjectMapper().readValue(eventStr, MenuItemEvent.class);
            log.info("Received menu item deletion event: {}", event.getMenuItemId());
            menuItemSearchRepository.deleteById(event.getMenuItemId());
            log.info("Successfully deleted menu item from index: {}", event.getMenuItemId());
        } catch (Exception e) {
            log.error("Error deleting menu item from index: {}", event.getMenuItemId(), e);
        }
    }
}
