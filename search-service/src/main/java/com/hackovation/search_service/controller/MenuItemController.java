package com.hackovation.search_service.controller;

import com.hackovation.search_service.dto.MenuItemRequest;
import com.hackovation.search_service.dto.MenuItemResponse;
import com.hackovation.search_service.service.MenuItemSearchService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/menu-items")
public class MenuItemController {

    @Autowired
    private MenuItemSearchService menuItemSearchService;

    @GetMapping
    public ResponseEntity<List<MenuItemResponse>> getAllMenuItems() throws Exception {
        return ResponseEntity.ok(menuItemSearchService.getAllMenuItems());
    }

    @GetMapping("/{menuItemId}")
    public ResponseEntity<MenuItemResponse> getMenuItemById(@PathVariable String menuItemId) {
        return menuItemSearchService.getMenuItemById(menuItemId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<MenuItemResponse> createMenuItem(@Valid @RequestBody MenuItemRequest request) {
        MenuItemResponse created = menuItemSearchService.createMenuItem(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{menuItemId}")
    public ResponseEntity<MenuItemResponse> updateMenuItem(
            @PathVariable String menuItemId,
            @Valid @RequestBody MenuItemRequest request) {
        return menuItemSearchService.updateMenuItem(menuItemId, request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{menuItemId}")
    public ResponseEntity<Void> deleteMenuItem(@PathVariable String menuItemId) {
        boolean deleted = menuItemSearchService.deleteMenuItem(menuItemId);
        return deleted
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
