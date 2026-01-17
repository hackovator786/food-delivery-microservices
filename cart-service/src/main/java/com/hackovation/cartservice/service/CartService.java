package com.hackovation.cartservice.service;

import com.hackovation.cartservice.dto.CartItemQtyResponse;
import com.hackovation.cartservice.dto.CartResponse;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

public interface CartService {
    CartResponse getCartItems(String userId);
    Map<String, Integer> getCartItemsAndQuantity(String userId, String restaurantId);

    @Transactional(rollbackFor = {Exception.class})
    CartItemQtyResponse updateCart(String userId, String restaurantId, String menuItemId, Boolean increase) throws Exception;
    Boolean deleteCart(String userId);
}
