package com.hackovation.orderservice.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class OrderItemDto {
    private String name;
    private Integer price;
    private Integer quantity;
}
