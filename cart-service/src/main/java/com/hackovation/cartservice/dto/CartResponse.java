package com.hackovation.cartservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {
    private String restaurantId;
    private BigDecimal subTotal;
    private BigDecimal tax;
    private BigDecimal discount;
    private BigDecimal total;
    private String notes;
    private List<CartItemResponse> items;
}
