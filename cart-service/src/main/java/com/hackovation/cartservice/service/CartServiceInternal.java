package com.hackovation.cartservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackovation.cartservice.dto.CartItemResponse;
import com.hackovation.cartservice.dto.CartResponseInternal;
import com.hackovation.cartservice.exception.ApiException;
import com.hackovation.cartservice.model.Cart;
import com.hackovation.cartservice.repository.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CartServiceInternal {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ObjectMapper objectMapper;

    public CartResponseInternal getCartItems(String userId) throws ApiException {

        // TODO - Check for updated prices from menu-service



        if(cartRepository.findByUserId(userId).isPresent()) {
            return convertCartToCartResponseInternal(cartRepository.findByUserId(userId).get());
        } else {
            throw new ApiException("Cart not found");
        }
    }

    private CartResponseInternal convertCartToCartResponseInternal(Cart cart) {
        CartResponseInternal cartResponse = new CartResponseInternal();
        cartResponse.setRestaurantId(cart.getRestaurantId());
        cartResponse.setSubTotal(cart.getSubTotal());
        cartResponse.setItems(cart.getItems().stream().map(item -> objectMapper.convertValue(item, CartItemResponse.class)).toList());
        cartResponse.setTotalAmount(calculateTotalAmount(cart));
        cartResponse.setTax(calculateTax(cart.getSubTotal()));
        cartResponse.setDiscount(calculateDiscount(cart.getSubTotal()));
        return cartResponse;
    }

    private Double calculateTotalAmount(Cart cart) {
         return cart.getSubTotal() + calculateTax(cart.getSubTotal()) - calculateDiscount(cart.getSubTotal());
    }

    private Double calculateTax(Double totalAmount) {
        return totalAmount * 0.18;
    }

    private Double calculateDiscount(Double totalAmount) {
        return totalAmount * 0.05;
    }
}
