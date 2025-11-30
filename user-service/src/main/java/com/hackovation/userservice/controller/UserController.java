package com.hackovation.userservice.controller;

import com.hackovation.userservice.dto.request.AddressRequest;
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
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserServiceImpl userService;

    @GetMapping("/info")
    public ResponseEntity<?> getUser(@RequestHeader("loggedInUser") String userId) throws ApiException {
        return ResponseEntity.ok().body(userService.getUser(userId));
    }
}
