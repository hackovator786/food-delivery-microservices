package com.hackovation.cartservice.service;

import com.hackovation.cartservice.model.Cart;
import com.hackovation.cartservice.model.CartItem;
import com.hackovation.cartservice.repository.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;


    public Cart getCart(String userId) {
        return cartRepository.findByUserId(userId);
    }

    public Cart addItemToCart(String userId, String menuItemId) {
        // Check menu item availability and retrieve details


        // Calculate price


        // Add item to cart
        Cart cart = getCart(userId);
//        cart.getItems().add(item);
//        cart.setTotalPrice(cart.getTotalPrice() + price);

        return cartRepository.save(cart);
    }

    public Cart removeItemFromCart(String userId, String productId) {
        Cart cart = getCart(userId);
        CartItem itemToRemove = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new ItemNotFoundException("Item not found in cart"));

        cart.getItems().remove(itemToRemove);
        cart.setTotalPrice(cart.getTotalPrice() - itemToRemove.getPrice());

        return cartRepository.save(cart);
    }

    public Cart updateItemQuantity(String userId, String productId, int quantity) {
        Cart cart = getCart(userId);
        CartItem itemToUpdate = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new ItemNotFoundException("Item not found in cart"));

        // Check product availability
        if (!inventoryService.isProductAvailable(productId, quantity)) {
            throw new ProductNotAvailableException("Product not available in required quantity");
        }

        // Retrieve product details
        Product product = productService.getProduct(productId);

        // Calculate price
        double oldPrice = itemToUpdate.getPrice();
        double newPrice = pricingService.calculatePrice(product, quantity);

        itemToUpdate.setQuantity(quantity);
        itemToUpdate.setPrice(newPrice);

        cart.setTotalPrice(cart.getTotalPrice() - oldPrice + newPrice);

        return cartRepository.save(cart);
    }

    public Cart checkout(String userId) {
        Cart cart = getCart(userId);
        if (cart.isCheckedOut()) {
            throw new CartAlreadyCheckedOutException("Cart has already been checked out");
        }

        // Proceed with order creation
        Order order = orderService.createOrder(cart);

        // Mark cart as checked out
        cart.setCheckedOut(true);
        cartRepository.save(cart);

        return cart;
    }
}
