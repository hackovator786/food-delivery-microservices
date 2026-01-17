package com.hackovation.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cart {
    private String restaurantId;
    private BigDecimal subTotal;
    private BigDecimal tax;
    private BigDecimal discount;
    private BigDecimal total;
    private String notes;
    private List<CartItem> items;
}
