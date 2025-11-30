package com.hackovation.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartResponse {
    private String restaurantId;
    private Double subTotal;
    private Double totalAmount;
    private Double tax;
    private Double discount;
    private List<CartItemResponse> items;
}
