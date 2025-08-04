package com.hackovation.userservice.controller;

import com.hackovation.userservice.dto.request.UserRequest;
import com.hackovation.userservice.dto.response.UserResponse;
import com.hackovation.userservice.exception.ApiException;
import com.hackovation.userservice.exception.RegException;
import com.hackovation.userservice.model.User;
import com.hackovation.userservice.service.UserServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    @Autowired
    private UserServiceImpl userService;

    @PostMapping("/create")
    public ResponseEntity<?> createUser(@Valid @RequestBody UserRequest userRequest) throws RegException {
        System.out.println(userRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(userRequest));
    }

    @GetMapping("/info")
    public ResponseEntity<?> getUser(@RequestHeader("loggedInUser") String userId) throws ApiException {
        return ResponseEntity.ok().body(userService.getUser(userId));
    }

    @PostMapping("/address")
    public String addAddress() {
        return "Address added";
    }

    @PutMapping
    public UserResponse updateUser(@RequestBody User user) {
        return null;
    }
}
