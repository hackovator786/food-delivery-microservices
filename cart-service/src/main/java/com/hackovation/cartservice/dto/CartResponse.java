package com.hackovation.cartservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {
    private String restaurantId;
    private Double subTotal;
    private Double tax;
    private Double total;
    private String notes;
    private List<CartItemResponse> items;
}
