package com.hackovation.menu_service.repository;

import com.hackovation.menu_service.model.MenuCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuCategoryRepository extends JpaRepository<MenuCategory, Long> {
    MenuCategory findByRestaurantIdAndName(String restaurantId, String name);

    Boolean existsByRestaurantIdAndName(String restaurantId, @NotBlank @Size(min = 2, max = 20, message = "Name must be between 2 and 20 characters") String name);
}
