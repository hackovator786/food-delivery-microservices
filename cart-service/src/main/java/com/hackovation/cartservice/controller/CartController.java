package com.hackovation.cartservice.controller;

import com.hackovation.cartservice.dto.AddItemRequest;
import com.hackovation.cartservice.dto.UpdateItemRequest;
import com.hackovation.cartservice.service.CartService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @PostMapping("/add")
    public ResponseEntity<?> addItemToCart(@RequestHeader("loggedInUser") String userId, @Valid @RequestBody AddItemRequest addItemRequest) throws Exception {
        return new ResponseEntity<>(cartService.updateCart(userId,
                addItemRequest.getRestaurantId(), addItemRequest.getMenuItemId(), true),
                HttpStatus.CREATED);
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateCart(@RequestHeader("loggedInUser") String userId, @Valid @RequestBody UpdateItemRequest updateItemRequest) throws Exception {
        return new ResponseEntity<>(cartService.updateCart(userId,
                updateItemRequest.getRestaurantId(), updateItemRequest.getMenuItemId(),
                updateItemRequest.getIncrease()),
                HttpStatus.CREATED);
    }

    @GetMapping("/get-items")
    public ResponseEntity<?> getCartItems(@RequestHeader("loggedInUser") String userId) {
        return ResponseEntity.ok(cartService.getCartItems(userId));
    }

}
