package com.hackovation.cartservice.service;

import com.hackovation.cartservice.dto.CartItemQtyResponse;
import com.hackovation.cartservice.dto.CartItemResponse;
import com.hackovation.cartservice.dto.CartResponse;
import com.hackovation.cartservice.model.Cart;
import com.hackovation.cartservice.model.CartItem;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static com.hackovation.cartservice.util.GenericUtils.bigDecimal;

public abstract class CartServiceAbstract {

    private static final Float TAX_PERCENT = 0.05f;
    private static final Float DISCOUNT_PERCENT = 0.0f;

    protected BigDecimal calculateTotalAmount(Cart cart) {
        return bigDecimal(cart.getSubTotal().add(calculateTax(cart.getSubTotal())).subtract(calculateDiscount(cart.getSubTotal())));
    }

    protected BigDecimal calculateTax(BigDecimal totalAmount) {
        return bigDecimal(totalAmount.multiply(new BigDecimal(TAX_PERCENT.toString())));
    }

    protected BigDecimal calculateDiscount(BigDecimal totalAmount) {
        return bigDecimal(totalAmount.multiply(new BigDecimal(DISCOUNT_PERCENT.toString())));
    }

    protected CartResponse convertCartToCartResponse(Cart cart) {
        CartResponse cartResponse = new CartResponse();
        cartResponse.setRestaurantId(cart.getRestaurantId());
        cartResponse.setSubTotal(cart.getSubTotal());
        cartResponse.setTax(calculateTax(cart.getSubTotal()));
        cartResponse.setDiscount(calculateDiscount(cart.getSubTotal()));
        cartResponse.setTotal(calculateTotalAmount(cart));
        cartResponse.setNotes(cart.getNotes());
        cartResponse.setItems(cart.getItems().stream().map(this::convertCartItemToCartItemResponse).toList());
        return cartResponse;
    }

    protected CartItemResponse convertCartItemToCartItemResponse(CartItem cartItem) {
        CartItemResponse cartItemResponse = new CartItemResponse();
        cartItemResponse.setMenuItemId(cartItem.getMenuItemId());
        cartItemResponse.setItemName(cartItem.getItemName());
        cartItemResponse.setQuantity(cartItem.getQuantity());
        cartItemResponse.setUnitPrice(cartItem.getUnitPrice());
        cartItemResponse.setLineItemTotal(bigDecimal(cartItem.getUnitPrice().multiply(new BigDecimal(cartItem.getQuantity().toString()))));
        return cartItemResponse;
    }

    protected CartItemQtyResponse convertCartToCartItemQtyResponse(Cart cart){
        CartItemQtyResponse cartItemQtyResponse = new CartItemQtyResponse();
        cartItemQtyResponse.setRestaurantId(cart.getRestaurantId());
        Map<String, Integer> menuItemIdQtyMap = new HashMap<>();
        cart.getItems().forEach(item -> menuItemIdQtyMap.put(item.getMenuItemId(), item.getQuantity()));
        cartItemQtyResponse.setItems(menuItemIdQtyMap);
        return cartItemQtyResponse;
    }
}
