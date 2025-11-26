package com.hackovation.cartservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemQtyResponse {
    String restaurantId = null;
    Map<String, Integer> items;
}
