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

    @PostMapping("/add-item/{restaurantId}")
    public ResponseEntity<?> addMenuItem(@RequestPart("menuItem") String menuItemString,
                              @RequestPart("file") MultipartFile file,
                              @PathVariable("restaurantId") String restaurantId, @RequestHeader("loggedInUser") String userId) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            MenuItemRequest menuItemRequest = objectMapper.readValue(menuItemString, MenuItemRequest.class);
            return ResponseEntity.status(HttpStatus.CREATED).body(menuItemService.addMenuItem(userId, restaurantId, menuItemRequest, file));
        } catch (JsonProcessingException e) {
            throw new ApiException("Invalid JSON format");
        } catch (Exception e) {
            e.printStackTrace();
            throw new ApiException("Unknown Error has occurred");
        }
    }

    @PostMapping("/add-category/{restaurantId}")
    public ResponseEntity<?> addMenuCategory(@RequestBody MenuCategoryRequest menuCategoryRequest,
                                             @PathVariable("restaurantId") String restaurantId) throws Exception {
        return ResponseEntity.status(HttpStatus.CREATED).body(menuItemService.addCategory(restaurantId,menuCategoryRequest));
    }

    @GetMapping("/{restaurantId}")
    public List<MenuItemResponse> getAllFoodItems(@PathVariable("restaurantId") String restaurantId) {
        return menuItemService.getAllFoodItems(restaurantId);
    }

    @PutMapping
    public String updateFoodItem(@RequestBody MenuItemResponse menuItemDto,
                                 @RequestHeader("loggedInUser") String username) {
        return menuItemService.updateFoodItem(menuItemDto, username);
    }

}
