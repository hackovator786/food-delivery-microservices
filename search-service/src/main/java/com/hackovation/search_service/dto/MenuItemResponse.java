package com.hackovation.search_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuItemResponse {
    private String menuItemId;
    private String restaurantId;
    private String menuItemName;
    private String description;
    private Double price;
    private Boolean isAvailable;
    private String imageUrl;
}
