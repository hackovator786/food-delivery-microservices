package com.hackovation.search_service.kafka;

import com.hackovation.search_service.dto.MenuItemEvent;
import com.hackovation.search_service.model.MenuItemDocument;
import com.hackovation.search_service.repository.MenuItemSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class MenuItemEventListener {

    private final MenuItemSearchRepository menuItemSearchRepository;

    @KafkaListener(
        topics = {"menu-item-created", "menu-item-updated"}, 
        groupId = "${spring.kafka.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleMenuItemCreatedOrUpdated(MenuItemEvent event) {
        try {
            log.info("Received menu item event: {}", event);
            
            // Convert tags to document format
            Set<MenuItemDocument.TagDoc> tagDocs = event.getTags().stream()
                .map(MenuItemDocument.TagDoc::new)
                .collect(Collectors.toSet());

            // Create or update document
            MenuItemDocument doc = menuItemSearchRepository.findById(event.getId())
                .orElse(new MenuItemDocument());
            
            doc.setId(event.getId());
            doc.setRestaurantId(event.getRestaurantId());
            doc.setName(event.getName());
            doc.setDescription(event.getDescription());
            doc.setPrice(event.getPrice());
            doc.setIsAvailable(event.isAvailable());
            doc.setTags(tagDocs);
            doc.setImageUrl(event.getImageUrl());
            doc.setUpdatedAt(Instant.now());
            
            if (doc.getCreatedAt() == null) {
                doc.setCreatedAt(Instant.now());
            }

            // Save to Elasticsearch
            menuItemSearchRepository.save(doc);
            log.info("Successfully processed menu item {}: {}", event.getEventType(), event.getId());
            
        } catch (Exception e) {
            log.error("Error processing menu item event: {}", event, e);
        }
    }

    @KafkaListener(
        topics = "menu-item-deleted",
        groupId = "${spring.kafka.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleMenuItemDeleted(MenuItemEvent event) {
        try {
            log.info("Received menu item deletion event: {}", event.getId());
            menuItemSearchRepository.deleteById(event.getId());
            log.info("Successfully deleted menu item from index: {}", event.getId());
        } catch (Exception e) {
            log.error("Error deleting menu item from index: {}", event.getId(), e);
        }
    }
}
