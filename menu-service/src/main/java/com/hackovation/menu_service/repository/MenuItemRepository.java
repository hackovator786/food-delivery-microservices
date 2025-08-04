package com.hackovation.menu_service.repository;

import com.hackovation.menu_service.model.MenuCategory;
import com.hackovation.menu_service.model.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    List<MenuItem> findByRestaurantId(String restaurantId);
    Boolean existsByRestaurantIdAndNameAndCategory(String name, String restaurantId, MenuCategory category);
}
