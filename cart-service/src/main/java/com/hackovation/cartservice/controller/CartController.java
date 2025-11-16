package com.hackovation.cartservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
public class CartController {

    @PostMapping("/add")
    public ResponseEntity<?> addItemToCart() {

        return null;
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateCart() {
        return null;
    }

    @GetMapping("/get-items")
    public ResponseEntity<?> getCartItems() {
        return null;
    }

}
