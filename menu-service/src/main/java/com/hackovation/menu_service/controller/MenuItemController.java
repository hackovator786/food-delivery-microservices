package com.hackovation.menu_service.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackovation.menu_service.dto.MenuCategoryRequest;
import com.hackovation.menu_service.dto.MenuItemResponse;
import com.hackovation.menu_service.dto.MenuItemRequest;
import com.hackovation.menu_service.exception.ApiException;
import com.hackovation.menu_service.service.MenuItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/menu")
@RequiredArgsConstructor
public class MenuItemController {
    @Autowired
    private MenuItemService menuItemService;

    @PostMapping("/add-item")
    public ResponseEntity<?> addMenuItem(@RequestPart("menuItem") String menuItemString,
                              @RequestPart("file") MultipartFile file,
                              @RequestHeader("restaurantId") String restaurantId, @RequestHeader("loggedInUser") String userId) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            System.out.println("MenuItemString: " + menuItemString);
            MenuItemRequest menuItemRequest = objectMapper.readValue(menuItemString, MenuItemRequest.class);
            System.out.println("MenuItemRequest: " + menuItemRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(menuItemService.addMenuItem(userId, restaurantId, menuItemRequest, file));
        } catch (JsonProcessingException e) {
            throw new ApiException("Invalid JSON format");
        } catch (Exception e) {
            e.printStackTrace();
            throw new ApiException("Unknown Error has occurred");
        }
    }

    @PostMapping("/add-category")
    public ResponseEntity<?> addMenuCategory(@RequestBody MenuCategoryRequest menuCategoryRequest,
                                             @RequestHeader("restaurantId") String restaurantId) throws Exception {
        return ResponseEntity.status(HttpStatus.CREATED).body(menuItemService.addCategory(restaurantId,menuCategoryRequest));
    }

    @GetMapping
    public List<MenuItemResponse> getAllFoodItems(@RequestHeader("restaurantId") String restaurantId) {
        return menuItemService.getAllMenuItems(restaurantId);
    }

    @PutMapping("/update-menu-item/{menuItemId}")
    public ResponseEntity<?> updateMenuItem(
            @PathVariable("menuItemId") String menuItemId,
            @RequestPart(value = "menuItem", required = false) String menuItemString,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestHeader("restaurantId") String restaurantId,
            @RequestHeader("loggedInUser") String userId) throws Exception {
        if (menuItemString == null) {
            throw new ApiException("Menu item data is required");
        }
        
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            MenuItemRequest menuItemRequest = objectMapper.readValue(menuItemString, MenuItemRequest.class);
            MenuItemResponse updatedItem = menuItemService.updateMenuItem(menuItemId, menuItemRequest, file, restaurantId, userId);
            return ResponseEntity.ok(updatedItem);
        } catch (JsonProcessingException e) {
            throw new ApiException("Invalid JSON format");
        } catch (Exception e) {
            e.printStackTrace();
            throw new ApiException("Error updating menu item: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete-menu-item/{menuItemId}")
    public ResponseEntity<?> deleteMenuItem(
            @PathVariable("menuItemId") String menuItemId,
            @RequestHeader("restaurantId") String restaurantId,
            @RequestHeader("loggedInUser") String userId) throws Exception {
        
        String response = menuItemService.deleteMenuItem(menuItemId, restaurantId, userId);
        System.out.println("Response: " + response);
        return ResponseEntity.noContent().build();
    }

}
