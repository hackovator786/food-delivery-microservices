package com.hackovation.menu_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuCategoryRequest {

    @NotBlank
    @Size(min = 2, max = 20, message = "Name must be between 2 and 20 characters")
    private String name;
    private Integer sortOrder;
}
