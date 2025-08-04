package com.hackovation.search_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemEvent {
    private String menuItemId;
    private String restaurantId;
    private String restaurantName;
    private String menuItemName;
    private String description;
    private String category;
    private Double price;
    private Boolean isAvailable;
    private String imageUrl;
    private Set<String> tags;
    private String eventType;
}
