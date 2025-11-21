package com.hackovation.cartservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemResponse {
    private String menuItemId;
    private String name;
    private Integer quantity;
    private Double unitPrice;
    private String notes;
}
