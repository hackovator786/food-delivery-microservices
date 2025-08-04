package com.hackovation.search_service.service;

import com.hackovation.search_service.dto.MenuItemRequest;
import com.hackovation.search_service.model.MenuItemDocument;
import com.hackovation.search_service.repository.MenuItemSearchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MenuItemIndexService {
    private final MenuItemSearchRepository repository;

    @Autowired
    public MenuItemIndexService(MenuItemSearchRepository repository) {
        this.repository = repository;
    }

    public void indexMenuItem(MenuItemRequest menuItemRequest) {
        Set<MenuItemDocument.TagDoc> tagDocs = menuItemRequest.getTags().stream()
            .map(MenuItemDocument.TagDoc::new)
            .collect(Collectors.toSet());

        MenuItemDocument doc = new MenuItemDocument();
        doc.setMenuItemId(menuItemRequest.getMenuItemId());
        doc.setRestaurantId(menuItemRequest.getRestaurantId());
        doc.setMenuItemName(menuItemRequest.getName());
        doc.setDescription(menuItemRequest.getDescription());
        doc.setPrice(menuItemRequest.getPrice());
        doc.setIsAvailable(menuItemRequest.getIsAvailable());
        doc.setTags(tagDocs);
        doc.setCreatedAt(Instant.now());
        doc.setUpdatedAt(Instant.now());
        doc.setImageUrl(menuItemRequest.getImageUrl());

        repository.save(doc);
    }

    public void deleteMenuItem(String menuItemId) {
        repository.deleteById(menuItemId);
    }
}
