package com.hackovation.search_service.dto;

import lombok.Data;

import java.util.List;

@Data
public class SearchRequestDto {
    private String q;
    private String restaurantId;
    private List<String> tags;
    private Double minPrice;
    private Double maxPrice;
    private Boolean isAvailable;

}
