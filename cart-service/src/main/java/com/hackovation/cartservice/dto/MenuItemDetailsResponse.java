package com.hackovation.cartservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Getter
@NoArgsConstructor
public class MenuItemDetailsResponse {
    private String menuItemName;
    private Double menuItemPrice;
}
