package com.hackovation.menu_service.controller;


import com.hackovation.menu_service.dto.MenuItemDetailsRequest;
import com.hackovation.menu_service.dto.MenuItemDetailsResponse;
import com.hackovation.menu_service.exception.ApiException;
import com.hackovation.menu_service.service.MenuItemInternalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class MenuItemInternalController {

    @Autowired
    private MenuItemInternalService menuItemInternalService;

    @PostMapping("/menu/get-menu-item")
    public ResponseEntity<?> getMenuItem(@Valid @RequestBody MenuItemDetailsRequest request) throws ApiException {
        return ResponseEntity.ok().body(menuItemInternalService.getMenuItem(request.getRestaurantId(), request.getMenuItemId()));
    }
}
