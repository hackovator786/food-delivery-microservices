package com.hackovation.search_service.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantResponse {
    private String name;
    private String city;
    private String imageUrl;
}
