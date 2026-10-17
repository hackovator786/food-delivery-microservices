package com.hackovation.restaurantservice.service;

import com.hackovation.restaurantservice.dto.AddressDto;
import com.hackovation.restaurantservice.dto.RestaurantEvent;
import com.hackovation.restaurantservice.dto.RestaurantRequest;
import com.hackovation.restaurantservice.dto.RestaurantResponse;
import com.hackovation.restaurantservice.exception.ApiException;
import com.hackovation.restaurantservice.model.Address;
import com.hackovation.restaurantservice.model.Restaurant;
import com.hackovation.restaurantservice.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RestaurantService {
    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private RestaurantEventPublisher restaurantEventPublisher;


    public RestaurantResponse addRestaurant(RestaurantRequest request, String userId) {
        String restaurantId = UUID.randomUUID().toString();
        Restaurant restaurant = Restaurant.builder()
                .restaurantId(restaurantId)
                .name(request.getName())
                .description(request.getDescription())
                .cuisine(request.getCuisine())
                .ownerId(userId)
                .address(Address.builder()
                        .doorNumber(request.getAddress().getDoorNumber())
                        .street(request.getAddress().getStreet())
                        .area(request.getAddress().getArea())
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
                        .doorNumber(savedRestaurant.getAddress().getDoorNumber())
                        .street(savedRestaurant.getAddress().getStreet())
                        .area(savedRestaurant.getAddress().getArea())
                        .city(savedRestaurant.getAddress().getCity())
                        .state(savedRestaurant.getAddress().getState())
                        .zipcode(savedRestaurant.getAddress().getZipcode())
                        .build())
                .build();

        restaurantEventPublisher.publishRestaurantCreated(RestaurantEvent.builder()
                .restaurantId(savedRestaurant.getRestaurantId())
                .name(savedRestaurant.getName())
                .description(savedRestaurant.getDescription())
                .cuisine(savedRestaurant.getCuisine())
                .city(savedRestaurant.getAddress().getCity())
                .eventType("CREATED")
                .build());

        return response;
    }

    public String getRestaurantName(String restaurantId, String userId) throws ApiException {
        String name = restaurantRepository.getNameByRestaurantIdAndOwnerId(restaurantId, userId);
        if (name == null) {
            throw new ApiException("Restaurant not found");
        }
        return name;
    }

    public String getRestaurantNameById(String restaurantId) throws ApiException {
        String name = restaurantRepository.getNameByRestaurantId(restaurantId);
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

    public List<RestaurantResponse> getAllRestaurants() {
        return restaurantRepository.findAll().stream()
                .map(r -> RestaurantResponse.builder()
                        .restaurantId(r.getRestaurantId())
                        .name(r.getName())
                        .description(r.getDescription())
                        .cuisine(r.getCuisine())
                        .address(AddressDto.builder()
                                .doorNumber(r.getAddress().getDoorNumber())
                                .street(r.getAddress().getStreet())
                                .area(r.getAddress().getArea())
                                .city(r.getAddress().getCity())
                                .state(r.getAddress().getState())
                                .zipcode(r.getAddress().getZipcode())
                                .build())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Publishes a restaurant-created Kafka event for every restaurant in the DB
     * so that the search-service can index them all into Elasticsearch.
     *
     * @return number of events published
     */
    public int uploadAllToSearch() {
        List<Restaurant> all = restaurantRepository.findAll();
        all.forEach(r -> restaurantEventPublisher.publishRestaurantCreated(
                RestaurantEvent.builder()
                        .restaurantId(r.getRestaurantId())
                        .name(r.getName())
                        .description(r.getDescription())
                        .cuisine(r.getCuisine())
                        .city(r.getAddress().getCity())
                        .avgRating(r.getAvgRating())
                        .eventType("CREATED")
                        .build()
        ));
        return all.size();
    }

}
