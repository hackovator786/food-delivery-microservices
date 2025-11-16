package com.hackovation.search_service.controller;

import com.hackovation.search_service.dto.SearchRequestDto;
import com.hackovation.search_service.model.MenuItemDocument;
import com.hackovation.search_service.model.RestaurantDocument;
import com.hackovation.search_service.service.MenuItemSearchService;
import com.hackovation.search_service.service.RestaurantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/search")
public class SearchController {

    @Autowired
    private MenuItemSearchService menuItemService;

    @Autowired
    private RestaurantService restaurantService;

    @GetMapping("/item")
    public Page<MenuItemDocument> searchMenuItems(
            @RequestParam(value = "menu-item-id", required = false) String menuItemId,
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "restaurant-id", required = false) String restaurantId,
            @RequestParam(value = "tags", required = false) List<String> tags,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "min-price", defaultValue = "0.0") Double minPrice,
            @RequestParam(value = "max-price", required = false) Double maxPrice,
            @RequestParam(value = "available", required = false) Boolean isAvailable,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) throws IOException {
        SearchRequestDto dto = SearchRequestDto.builder()
                .menuItemId(menuItemId)
                .query(query)
                .restaurantId(restaurantId)
                .tags(tags)
                .category(category)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .isAvailable(isAvailable)
                .build();

        return menuItemService.searchMenuItems(dto, PageRequest.of(page, size));
    }

    @GetMapping("/all-items")
    public List<MenuItemDocument> getAllMenuItems() throws IOException {
        return menuItemService.getAllMenuItems();
    }

    @GetMapping("/all-restaurants")
    public List<RestaurantDocument> getAllRestaurants() throws IOException {
        return restaurantService.getAllRestaurants();
    }
}
