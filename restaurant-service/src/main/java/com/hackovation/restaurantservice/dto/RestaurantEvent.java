package com.hackovation.restaurantservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantEvent {
    private String restaurantId;
    private String name;
    private String description;
    private String cuisine;
    private String city;
    private String imageUrl;
    private Double avgRating;
    private String eventType; // "CREATED", "UPDATED"
}
