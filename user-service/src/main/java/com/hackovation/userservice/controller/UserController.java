package com.hackovation.userservice.controller;

import com.hackovation.userservice.dto.request.UserRequest;
import com.hackovation.userservice.exception.ApiException;
import com.hackovation.userservice.exception.RegException;
import com.hackovation.userservice.service.UserServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class UserController {
    @Autowired
    private UserServiceImpl userService;

    @PostMapping("/internal/user/create")
    public ResponseEntity<?> createUser(@Valid @RequestBody UserRequest userRequest) throws RegException, ApiException {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(userRequest));
    }

    @GetMapping("/user/info")
    public ResponseEntity<?> getUser(@RequestHeader("loggedInUser") String userId) throws ApiException {
        return ResponseEntity.ok().body(userService.getUser(userId));
    }

    @PostMapping("/user/address")
    public String addAddress() {
        return "Address added";
    }

    @PutMapping("/internal/user/update")
    public ResponseEntity<?> updateUser(@Valid @RequestBody UserRequest userRequest) throws ApiException {
        return ResponseEntity.ok().body(userService.updateUser(userRequest));
    }
}
