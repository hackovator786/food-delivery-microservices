package com.hackovation.menu_service.repository;

import com.hackovation.menu_service.dto.MenuItemDetailsResponse;
import com.hackovation.menu_service.model.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

import java.util.Optional;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    List<MenuItem> findByRestaurantId(String restaurantId);
    Optional<MenuItem> findByMenuItemId(String menuItemId);

    @Query("SELECT new com.hackovation.menu_service.dto.MenuItemDetailsResponse(m.menuItemName, m.price) " +
            "FROM MenuItem m WHERE m.restaurantId = :restaurantId AND m.menuItemId = :menuItemId")
    Optional<MenuItemDetailsResponse> findByRestaurantIdAndMenuItemId(@Param("restaurantId") String restaurantId, @Param("menuItemId") String menuItemId);
}
