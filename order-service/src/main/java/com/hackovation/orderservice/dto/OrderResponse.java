package com.hackovation.orderservice.dto;

import com.hackovation.orderservice.model.Address;
import com.hackovation.orderservice.model.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponse {
    private Long orderNumber;
    private String restaurantName;
    private String userName;
    private List<OrderItemDto> orderItems;
    private BigDecimal totalAmount;
    private Long orderTime;
    private OrderStatus orderStatus;
    private Long expectedDeliveryTime;
    private Address address;
    private String paymentId;
}
