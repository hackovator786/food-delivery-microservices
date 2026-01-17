package com.hackovation.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {
    private String menuItemId;
    private String itemName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineItemTotal;
}
