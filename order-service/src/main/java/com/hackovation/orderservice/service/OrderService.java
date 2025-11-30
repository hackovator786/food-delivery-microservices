package com.hackovation.orderservice.service;

import com.hackovation.orderservice.dto.OrderItemDto;
import com.hackovation.orderservice.dto.OrderResponse;
import com.hackovation.orderservice.event.OrderPlacedNotification;
import com.hackovation.orderservice.model.Order;
import com.hackovation.orderservice.model.OrderItem;
import com.hackovation.orderservice.model.OrderStatus;
import com.hackovation.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;

    private final KafkaTemplate<String, OrderPlacedNotification> kafkaTemplate;

    public OrderResponse createOrder(Order order) {
        // Get the items from the cart

        return null;
    }

    public void updateOrderAfterPayment(List<String> paymentInfo) {
        // TODO
        return;
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
        kafkaTemplate.send("order-restaurant-notification-topic", notification);
    }

    public void updateOrderStatus(String orderId, OrderStatus orderStatus) {
        Optional<Order> optionalOrder = orderRepository.findByOrderId(orderId);
        optionalOrder.ifPresent(order -> order.setOrderStatus(orderStatus));
    }
}
