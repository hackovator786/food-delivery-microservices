package com.hackovation.restaurantservice.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RestaurantDto {
    private String restaurantId;
    private String name;
    private String description;
    private String cuisine;
    private AddressDto address;
    private Double rating;
}
