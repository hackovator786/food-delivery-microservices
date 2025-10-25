package com.hackovation.authservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "RESTAURANT-SERVICE", configuration = FeignConfig.class, path = "/api/v1.0/internal")
public interface RestaurantInterface {
    @GetMapping("/restaurant-id")
    public ResponseEntity<?> getRestaurantId(@RequestParam("user-id") String userId);
}
