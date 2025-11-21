package com.hackovation.menu_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MenuItemDetailsRequest {
    @NotBlank
    @Size(min = 2, max = 60, message = "Restaurant Id cannot be empty")
    private String restaurantId;

    @NotBlank
    @Size(min = 2, max = 60, message = "Menu Item Id cannot be empty")
    private String menuItemId;
}
