package com.hackovation.orderservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackovation.orderservice.dto.*;
import com.hackovation.orderservice.event.OrderPlacedNotification;
import com.hackovation.orderservice.feign.CartInterface;
import com.hackovation.orderservice.feign.FeignExceptionWrapper;
import com.hackovation.orderservice.model.Order;
import com.hackovation.orderservice.model.OrderItem;
import com.hackovation.orderservice.model.OrderStatus;
import com.hackovation.orderservice.model.PaymentStatus;
import com.hackovation.orderservice.repository.OrderRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartInterface cartInterface;

    @Autowired
    private ObjectMapper objectMapper;

//    private final KafkaTemplate<String, OrderPlacedNotification> kafkaTemplate;

    public Cart createOrder(String userId) throws Exception {
        try {
            // Get the items from the cart
            Cart cart = getItemsFromCartService(userId);
            if(cart == null) {
                throw new Exception("Unknown error has occurred");
            }

            // Create an order entity
//            Order order = createOrderEntity(userId,cart);
//            orderRepository.save(order);

            // Create a payment order


        } catch (Exception e){
            throw e;
        }
    }

    public void updateOrderAfterPayment(List<String> paymentInfo) {
        // TODO
        return;
    }

    public void updateOrderStatus(String orderId, OrderStatus orderStatus) {
        Optional<Order> optionalOrder = orderRepository.findByOrderId(orderId);
        optionalOrder.ifPresent(order -> order.setOrderStatus(orderStatus));
    }

    private Cart getItemsFromCartService(String userId) throws Exception {
        try {
            ResponseEntity<?> response = cartInterface.getItemsInCart(userId);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() instanceof Map<?, ?> map) {
                return objectMapper.convertValue(map, Cart.class);
            } else if (response.getBody() instanceof Map<?, ?> map) {
                ErrorResponse error = objectMapper.convertValue(map, ErrorResponse.class);
                System.out.println("Error: " + error.getMessage());
            } else {
                System.out.println("Unexpected response format");
            }
        } catch (FeignException feignException) {
            System.out.println("Error from cart service: " + feignException.getMessage());
        } catch (FeignExceptionWrapper ex) {
            ErrResponse error = ex.getErrorResponse();
            System.out.println("Error from cart service: " + error.getMessage());
        } catch (Exception e){
            e.printStackTrace();
            throw e;
        }
        return null;
    }

    private Order createOrderEntity(String userId, Cart cart){
        Order order = Order.builder()
                .orderId(UUID.randomUUID().toString())
                .userId(userId)
                .restaurantId(cart.getRestaurantId())
                .totalAmount(cart.getTotal())
                .subTotal(cart.getSubTotal())
                .deliveryCharge(new BigDecimal("0.0"))
                .tax(cart.getTax())
                .discount(cart.getDiscount())
                .address(null)
                .expectedDeliveryTime(60L)
                .orderStatus(OrderStatus.PAYMENT_PENDING)
                .paymentStatus(PaymentStatus.PENDING)
                .build();
        cart.getItems().forEach(cartItem -> order.addOrderItem(convertCartItemToOrderItem(cartItem)));
        return order;
    }

    private OrderItem convertCartItemToOrderItem(CartItem cartItem){
        return OrderItem.builder()
                .menuItemId(cartItem.getMenuItemId())
                .itemName(cartItem.getItemName())
                .quantity(cartItem.getQuantity())
                .unitPriceSnapshot(cartItem.getUnitPrice())
                .build();
    }

    private void sendNotificationToRestaurant(Order order) {
        // Send a notification to Restaurant Service to prepare this order
        OrderPlacedNotification notification = OrderPlacedNotification.builder()
                .orderId(order.getOrderId())
                .userId(order.getUserId())
                .foodItemIds(order.getOrderItems().stream()
                        .map(OrderItem::getMenuItemId).toList())
                .foodItemQuantities(order.getOrderItems().stream()
                        .map(OrderItem::getQuantity).toList())
                .build();
//        kafkaTemplate.send("order-restaurant-notification-topic", notification);
    }
}
