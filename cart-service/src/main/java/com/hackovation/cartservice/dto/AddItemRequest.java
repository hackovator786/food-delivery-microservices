package com.hackovation.cartservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddItemRequest {

    @NotBlank
    @Size(min = 2, max = 60)
    private String restaurantId;

    @NotBlank
    @Size(min = 2, max = 60)
    private String menuItemId;

    private Boolean increase;
}
