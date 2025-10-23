package com.hackovation.restaurantservice.controller;

import com.hackovation.restaurantservice.dto.RestaurantRequest;
import com.hackovation.restaurantservice.exception.ApiException;
import com.hackovation.restaurantservice.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequestMapping("/restaurant")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;
    @PostMapping("/create")
    public ResponseEntity<?> addRestaurant(@RequestBody RestaurantRequest request,
                                        @RequestHeader("loggedInUser") String username) {
        return ResponseEntity.status(HttpStatus.CREATED).body(restaurantService.addRestaurant(request, username));
    }

//    @GetMapping
//    public List<RestaurantResponse> getAllRestaurants() {
//        return restaurantService.getAllRestaurants();
//    }
//
    @GetMapping
    public ResponseEntity<?> getRestaurantName(@RequestParam("restaurant-id") String restaurantId, @RequestParam("user-id") String userId) throws ApiException {
        return ResponseEntity.ok().body(Map.of("name", restaurantService.getRestaurantName(restaurantId, userId)));
    }

    @GetMapping("/test")
    public ResponseEntity<?> test(@RequestHeader("loggedInUser") String username) {
        return ResponseEntity.ok().body("test --> " + username);
    }
}
