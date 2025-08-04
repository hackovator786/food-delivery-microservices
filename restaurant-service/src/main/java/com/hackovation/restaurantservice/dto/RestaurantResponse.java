package com.hackovation.restaurantservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RestaurantResponse {
    private String restaurantId;
    private String name;
    private String description;
    private String cuisine;
    private AddressDto address;
}
