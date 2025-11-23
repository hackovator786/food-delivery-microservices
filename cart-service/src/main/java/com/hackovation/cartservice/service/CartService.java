package com.hackovation.cartservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackovation.cartservice.dto.*;
import com.hackovation.cartservice.exception.ApiException;
import com.hackovation.cartservice.feign.FeignExceptionWrapper;
import com.hackovation.cartservice.feign.MenuItemInterface;
import com.hackovation.cartservice.model.Cart;
import com.hackovation.cartservice.model.CartItem;
import com.hackovation.cartservice.repository.CartRepository;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private MenuItemInterface menuItemInterface;

    @Autowired
    private ObjectMapper objectMapper;

    public CartResponse getCartItems(String userId) {

        // Check for updated prices from menu-service
        if(cartRepository.findByUserId(userId).isPresent())
            return convertCartToCartResponse(cartRepository.findByUserId(userId).get());

        return null;
    }

    @Transactional(rollbackFor = {Exception.class})
    public Integer updateCart(String userId, String restaurantId, String menuItemId, Boolean increase) throws Exception {

        Optional<Cart> optionalCart = cartRepository.findByUserId(userId);
        if(optionalCart.isEmpty()) {
            if(!increase) {
                throw new ApiException("Cart does not exist");
            }
            Cart cart = new Cart(userId, restaurantId);
            MenuItemDetailsResponse menuItemDetailsResponse = getMenuItemDetails(restaurantId, menuItemId);
            CartItem cartItem = CartItem.builder()
                    .menuItemId(menuItemId)
                    .name(menuItemDetailsResponse.getMenuItemName())
                    .quantity(1)
                    .unitPrice(menuItemDetailsResponse.getMenuItemPrice())
                    .build();
            cart.setItems(List.of(cartItem));
            cart.setTotalAmount(cartItem.getUnitPrice());
            return cartRepository.save(cart).getItems().size();
        }

        Cart cart = optionalCart.get();

        if(!cart.getRestaurantId().equals(restaurantId)) {
            throw new ApiException("Restaurant id mismatch");
        }

        boolean itemExists = cart.getItems().stream().anyMatch(item -> item.getMenuItemId().equals(menuItemId));

        if(!itemExists) {
            if(!increase) {
                throw new ApiException("Item does not exist in cart");
            }
            MenuItemDetailsResponse menuItemDetailsResponse = getMenuItemDetails(restaurantId, menuItemId);
            CartItem cartItem = CartItem.builder()
                    .menuItemId(menuItemId)
                    .name(menuItemDetailsResponse.getMenuItemName())
                    .quantity(1)
                    .unitPrice(menuItemDetailsResponse.getMenuItemPrice())
                    .build();
            cart.getItems().add(cartItem);
            cart.setTotalAmount(cart.getTotalAmount() + cartItem.getUnitPrice());
            return cartRepository.save(cart).getItems().size();
        }

        if(increase) {
            CartItem cartItem = cart.getItems().stream().filter(item -> item.getMenuItemId().equals(menuItemId)).findFirst().get();
            cartItem.setQuantity(cartItem.getQuantity() + 1);
            cart.setTotalAmount(cart.getTotalAmount() + cartItem.getUnitPrice());
            return cartRepository.save(cart).getItems().size();
        }

        CartItem cartItem = cart.getItems().stream().filter(item -> item.getMenuItemId().equals(menuItemId)).findFirst().get();
        cartItem.setQuantity(cartItem.getQuantity() - 1);
        cart.setTotalAmount(cart.getTotalAmount() - cartItem.getUnitPrice());
        if(cartItem.getQuantity() == 0) {
            cart.getItems().remove(cartItem);
        }

        if(cart.getItems().isEmpty()) {
            cartRepository.delete(cart);
            return null;
        }

        return cartRepository.save(cart).getItems().size();
    }

    public Boolean deleteCart(String userId) {
        return null;
    }

    private MenuItemDetailsResponse getMenuItemDetails(String restaurantId, String menuItemId) throws Exception {
        try{
            ResponseEntity<?> response = menuItemInterface.getMenuItem(new MenuItemDetailsRequest(restaurantId, menuItemId));
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() instanceof Map<?, ?> map) {
                 return objectMapper.convertValue(map, MenuItemDetailsResponse.class);
            } else if (response.getBody() instanceof Map<?, ?> map) {
                ErrorResponse error = objectMapper.convertValue(map, ErrorResponse.class);
                System.out.println("Error: " + error.getMessage());
                throw new ApiException(error.getMessage());
            } else {
                System.out.println("Unexpected response format");
                throw new Exception("Unexpected response format");
            }
        } catch (FeignException feignException) {
            System.out.println("Error from menu service: " + feignException.getMessage());
            throw new ApiException(feignException.getMessage());
        } catch (FeignExceptionWrapper ex) {
            ErrResponse error = ex.getErrorResponse();
            System.out.println("Error from menu service: " + error.getMessage());
            throw new ApiException(error.getMessage());
        } catch (Exception ex){
            throw new Exception(ex.getMessage());
        }
    }

    private CartResponse convertCartToCartResponse(Cart cart) {
        CartResponse cartResponse = new CartResponse();
        cartResponse.setRestaurantId(cart.getRestaurantId());
        cartResponse.setTotalAmount(cart.getTotalAmount());
        cartResponse.setItems(cart.getItems().stream().map(this::convertCartItemToCartItemResponse).toList());
        return cartResponse;
    }

    private CartItemResponse convertCartItemToCartItemResponse(CartItem cartItem) {
        CartItemResponse cartItemResponse = new CartItemResponse();
        cartItemResponse.setMenuItemId(cartItem.getMenuItemId());
        cartItemResponse.setName(cartItem.getName());
        cartItemResponse.setQuantity(cartItem.getQuantity());
        cartItemResponse.setUnitPrice(cartItem.getUnitPrice());
        cartItemResponse.setNotes(cartItem.getNotes());
        return cartItemResponse;
    }
}
