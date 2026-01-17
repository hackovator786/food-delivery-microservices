package com.hackovation.cartservice.controller;

import com.hackovation.cartservice.dto.AddItemRequest;
import com.hackovation.cartservice.dto.UpdateItemRequest;
import com.hackovation.cartservice.service.CartServiceImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartServiceImpl cartServiceImpl;

    @PostMapping("/add")
    public ResponseEntity<?> addItemToCart(@RequestHeader("loggedInUser") String userId, @Valid @RequestBody AddItemRequest addItemRequest) throws Exception {
        return new ResponseEntity<>(cartServiceImpl.updateCart(userId,
                addItemRequest.getRestaurantId(), addItemRequest.getMenuItemId(), true),
                HttpStatus.CREATED);
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateCart(@RequestHeader("loggedInUser") String userId, @Valid @RequestBody UpdateItemRequest updateItemRequest) throws Exception {
        return new ResponseEntity<>(cartServiceImpl.updateCart(userId,
                updateItemRequest.getRestaurantId(), updateItemRequest.getMenuItemId(),
                updateItemRequest.getIncrease()),
                HttpStatus.OK);
    }

    @GetMapping("/get-items")
    public ResponseEntity<?> getCartItems(@RequestHeader("loggedInUser") String userId) {
        return ResponseEntity.ok(cartServiceImpl.getCartItems(userId));
    }

    @GetMapping("/get-items-quantity")
    public ResponseEntity<?> getCartItemQuantity(@RequestHeader("loggedInUser") String userId, @RequestParam("restaurant-id") String restaurantId) {
        return ResponseEntity.ok(cartServiceImpl.getCartItemsAndQuantity(userId, restaurantId));
    }

    // TODO - Query to be optimized for this API
    @GetMapping("/get-items-count")
    public ResponseEntity<?> getCartItemsCount(@RequestHeader("loggedInUser") String userId) {
        return ResponseEntity.ok(Map.of("cartItemsCount", cartServiceImpl.getCartItems(userId) != null ? cartServiceImpl.getCartItems(userId).getItems().size() : 0));
    }

}
