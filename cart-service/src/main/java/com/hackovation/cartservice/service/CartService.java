package com.hackovation.cartservice.service;

import com.hackovation.cartservice.model.Cart;
import com.hackovation.cartservice.repository.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    public Cart getCart(String userId) {

        // Check for updated prices from menu-service

        return cartRepository.findByUserId(userId);
    }

    public Cart updateCart(String userId, String menuItemId, Boolean increase) {

        // Validate menuItemId if it is not present in the cart


        return null;
    }

    public Boolean deleteCart(String userId) {
        return null;
    }
}
