package com.hackovation.restaurantservice.service;

import com.hackovation.restaurantservice.dto.AddressDto;
import com.hackovation.restaurantservice.dto.RestaurantRequest;
import com.hackovation.restaurantservice.dto.RestaurantResponse;
import com.hackovation.restaurantservice.exception.ApiException;
import com.hackovation.restaurantservice.model.Address;
import com.hackovation.restaurantservice.model.Restaurant;
import com.hackovation.restaurantservice.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RestaurantService {
    @Autowired
    private RestaurantRepository restaurantRepository;


    public RestaurantResponse addRestaurant(RestaurantRequest request, String userId) {
        String restaurantId = UUID.randomUUID().toString();
        Restaurant restaurant = Restaurant.builder()
                .restaurantId(restaurantId)
                .name(request.getName())
                .description(request.getDescription())
                .cuisine(request.getCuisine())
                .ownerId(userId)
                .address(Address.builder()
                        .fullAddress(request.getAddress().getFullAddress())
                        .city(request.getAddress().getCity())
                        .state(request.getAddress().getState())
                        .zipcode(request.getAddress().getZipcode())
                        .build())
                .build();
        Restaurant savedRestaurant = restaurantRepository.save(restaurant);
        RestaurantResponse response = RestaurantResponse.builder()
                .restaurantId(savedRestaurant.getRestaurantId())
                .name(savedRestaurant.getName())
                .description(savedRestaurant.getDescription())
                .cuisine(savedRestaurant.getCuisine())
                .address(AddressDto.builder()
                        .fullAddress(savedRestaurant.getAddress().getFullAddress())
                        .city(savedRestaurant.getAddress().getCity())
                        .state(savedRestaurant.getAddress().getState())
                        .zipcode(savedRestaurant.getAddress().getZipcode())
                        .build())
                .build();
        return response;
    }

    public String getRestaurantName(String restaurantId, String userId) throws ApiException {
        String name = restaurantRepository.getNameByRestaurantIdAndOwnerId(restaurantId, userId);
        if (name == null) {
            throw new ApiException("Restaurant not found");
        }
        return name;
    }

    public String getRestaurantId(String userId) throws ApiException{
        String restaurantId = restaurantRepository.getRestaurantIdByOwnerId(userId); // userId is the ownerId
        System.out.println("Inside Restaurant service --> Restaurant Id: " + restaurantId);
        return restaurantId;
    }


}
