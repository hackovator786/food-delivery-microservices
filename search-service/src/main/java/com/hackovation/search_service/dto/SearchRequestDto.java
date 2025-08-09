package com.hackovation.search_service.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SearchRequestDto {
    private String menuItemId;
    private String query;
    private String restaurantId;
    private List<String> tags;
    private String category;
    private Double minPrice;
    private Double maxPrice;
    private Boolean isAvailable;

}
