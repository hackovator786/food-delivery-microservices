package com.hackovation.restaurantservice.controller;

import com.hackovation.restaurantservice.dto.RestaurantRequest;
import com.hackovation.restaurantservice.dto.RestaurantResponse;
import com.hackovation.restaurantservice.exception.ApiException;
import com.hackovation.restaurantservice.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;
    @PostMapping("/restaurant/create")
    public ResponseEntity<?> addRestaurant(@RequestBody RestaurantRequest request,
                                        @RequestHeader("loggedInUser") String userId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(restaurantService.addRestaurant(request, userId));
    }

    @GetMapping("/restaurant")
    public ResponseEntity<List<RestaurantResponse>> getAllRestaurants() {
        return ResponseEntity.ok(restaurantService.getAllRestaurants());
    }

    @GetMapping("/internal/restaurant-name")
    public ResponseEntity<?> getRestaurantName(@RequestParam("restaurant-id") String restaurantId, @RequestParam("user-id") String userId) throws ApiException {
        return ResponseEntity.ok().body(Map.of("name", restaurantService.getRestaurantName(restaurantId, userId)));
    }

    @GetMapping("/internal/restaurant-name-only")
    public ResponseEntity<?> getRestaurantNameById(@RequestParam("restaurant-id") String restaurantId) throws ApiException {
        return ResponseEntity.ok().body(Map.of("name", restaurantService.getRestaurantNameById(restaurantId)));
    }

    @GetMapping("/internal/restaurant-id")
    public ResponseEntity<?> getRestaurantId(@RequestParam("user-id") String userId) throws ApiException {
        return ResponseEntity.ok().body(Map.of("restaurantId", restaurantService.getRestaurantId(userId)));
    }

    @PostMapping("/restaurant/upload-all")
    public ResponseEntity<?> uploadAllToSearch() {
        int count = restaurantService.uploadAllToSearch();
        return ResponseEntity.ok(Map.of("published", count));
    }

    @GetMapping("/restaurant/test")
    public ResponseEntity<?> test(@RequestHeader("loggedInUser") String username, @RequestHeader("restaurantId") String restaurantId) {
        return ResponseEntity.ok().body("test --> " + username + " restaurant Id ---> " + restaurantId);
    }
}
