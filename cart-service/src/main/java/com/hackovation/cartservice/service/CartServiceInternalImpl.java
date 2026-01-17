package com.hackovation.cartservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackovation.cartservice.dto.CartItemResponse;
import com.hackovation.cartservice.dto.CartResponseInternal;
import com.hackovation.cartservice.exception.ApiException;
import com.hackovation.cartservice.model.Cart;
import com.hackovation.cartservice.model.CartItem;
import com.hackovation.cartservice.repository.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import static com.hackovation.cartservice.util.GenericUtils.bigDecimal;

@Service
public class CartServiceInternalImpl extends CartServiceAbstract implements CartServiceInternal{

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
        cartResponse.setItems(cart.getItems().stream().map(this::convertCartItemToCartItemResponse).toList());
        cartResponse.setTax(calculateTax(bigDecimal(cart.getSubTotal().toString())));
        cartResponse.setDiscount(calculateDiscount(cart.getSubTotal()));
        cartResponse.setTotal(calculateTotalAmount(cart));
        return cartResponse;
    }
}
