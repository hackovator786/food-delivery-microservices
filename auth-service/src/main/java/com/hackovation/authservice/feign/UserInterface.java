package com.hackovation.authservice.feign;

import com.hackovation.authservice.dto.request.UserRequest;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "USER-SERVICE", configuration = FeignConfig.class)
public interface UserInterface {
    @PostMapping("/api/v1.0/user/create")
    public ResponseEntity<?> createUser(@Valid @RequestBody UserRequest userRequest);
}
