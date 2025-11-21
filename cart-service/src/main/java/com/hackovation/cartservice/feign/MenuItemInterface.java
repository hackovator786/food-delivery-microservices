package com.hackovation.cartservice.feign;


import com.hackovation.cartservice.dto.MenuItemDetailsRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "MENU-SERVICE", configuration = FeignConfig.class, path = "/api/v1.0/internal")
public interface MenuItemInterface {

    @PostMapping("/menu/get-menu-item")
    public ResponseEntity<?> getMenuItem(@RequestBody MenuItemDetailsRequest request);
}
