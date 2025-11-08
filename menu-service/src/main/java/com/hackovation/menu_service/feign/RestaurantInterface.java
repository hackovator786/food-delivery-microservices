package com.hackovation.menu_service.feign;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "RESTAURANT-SERVICE", configuration = FeignConfig.class)
public interface RestaurantInterface {
    @GetMapping("/api/v1.0/internal/restaurant-name")
    public ResponseEntity<?> getRestaurantName(@RequestParam("restaurant-id") String restaurantId, @RequestParam("user-id") String userId);
}
