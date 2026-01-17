package com.hackovation.cartservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemResponse {
    private String menuItemId;
    private String itemName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineItemTotal;
}
