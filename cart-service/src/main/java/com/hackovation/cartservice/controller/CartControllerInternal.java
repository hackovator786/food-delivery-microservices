package com.hackovation.cartservice.controller;

import com.hackovation.cartservice.exception.ApiException;
import com.hackovation.cartservice.service.CartServiceInternalImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/cart")
public class CartControllerInternal {

    @Autowired
    private CartServiceInternalImpl cartServiceInternalImpl;

    @GetMapping("/get-items")
    public ResponseEntity<?> getCartItems(@RequestParam("user-id") String userId) throws ApiException {
        return ResponseEntity.ok(cartServiceInternalImpl.getCartItems(userId));
    }

}
