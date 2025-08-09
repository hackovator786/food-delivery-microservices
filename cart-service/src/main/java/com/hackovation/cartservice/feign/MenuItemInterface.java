package com.hackovation.cartservice.feign;


import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "MENU-SERVICE", configuration = FeignConfig.class)
public interface MenuItemInterface {

}
