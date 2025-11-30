package com.hackovation.cartservice.controller;

import com.hackovation.cartservice.dto.AddItemRequest;
import com.hackovation.cartservice.dto.UpdateItemRequest;
import com.hackovation.cartservice.service.CartService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/internal/cart")
public class CartControllerInternal {

    @Autowired
    private CartService cartService;

    @GetMapping("/get-items")
    public ResponseEntity<?> getCartItems(@RequestParam("user-id") String userId) {
        return ResponseEntity.ok(cartService.getCartItems(userId));
    }

}
