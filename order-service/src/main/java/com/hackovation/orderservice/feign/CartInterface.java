package com.hackovation.orderservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "CART-SERVICE", configuration = FeignConfig.class, path = "/api/v1.0/internal/cart")
public interface CartInterface {
    @GetMapping("/get-items")
    public ResponseEntity<?> getItemsInCart(@RequestParam("user-id") String userId);
}
