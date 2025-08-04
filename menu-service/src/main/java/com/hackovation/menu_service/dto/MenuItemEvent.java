package com.hackovation.menu_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemEvent {
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("restaurantId")
    private String restaurantId;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("price")
    private BigDecimal price;
    
    @JsonProperty("category")
    private String category;
    
    @JsonProperty("imageUrl")
    private String imageUrl;
    
    @JsonProperty("isAvailable")
    private boolean isAvailable;
    
    @JsonProperty("tags")
    private Set<String> tags;
    
    @JsonProperty("eventType")
    private String eventType; // "CREATED", "UPDATED", "DELETED"
}
