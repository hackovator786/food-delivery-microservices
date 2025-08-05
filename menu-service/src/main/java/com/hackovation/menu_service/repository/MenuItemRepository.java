package com.hackovation.menu_service.repository;

import com.hackovation.menu_service.model.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

import java.util.Optional;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    List<MenuItem> findByRestaurantId(String restaurantId);
    Optional<MenuItem> findByMenuItemId(String menuItemId);
}
