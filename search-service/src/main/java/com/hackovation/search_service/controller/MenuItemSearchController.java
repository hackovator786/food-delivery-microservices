// src/main/java/com/yourorg/search/controller/MenuItemSearchController.java
package com.hackovation.search_service.controller;

import com.hackovation.search_service.dto.MenuItemRequest;
import com.hackovation.search_service.service.MenuItemIndexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/search/menu-items")
public class MenuItemSearchController {

    @Autowired
    private MenuItemIndexService menuItemIndexService;

    @PostMapping
    public String addMenuItem(@RequestBody MenuItemRequest menuItemRequest) {
        menuItemIndexService.indexMenuItem(menuItemRequest);
        return "MenuItem added";
    }
}
