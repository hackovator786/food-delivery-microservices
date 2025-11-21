package com.hackovation.menu_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class MenuItemDetailsResponse {
    private String menuItemName;
    private Double menuItemPrice;
}
