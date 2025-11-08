package com.hackovation.restaurantservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RestaurantRequest {

    @NotBlank(message = "Restaurant name cannot be blank")
    @Pattern(regexp = "^[a-zA-Z0-9 ]{2,40}$", message = "Invalid name")
    private String name;
    private String description;
    private String cuisine;
    private AddressDto address;
}
