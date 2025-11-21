package com.hackovation.menu_service.service;

import com.hackovation.menu_service.dto.MenuItemDetailsResponse;
import com.hackovation.menu_service.exception.ApiException;
import com.hackovation.menu_service.repository.MenuItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MenuItemInternalService {

    @Autowired
    private MenuItemRepository menuItemRepository;

    public MenuItemDetailsResponse getMenuItem(String restaurantId, String menuItemId) throws ApiException {
        return menuItemRepository.findByRestaurantIdAndMenuItemId(restaurantId, menuItemId)
                .orElseThrow(() -> new ApiException("MenuItem does not exist"));

    }
}
