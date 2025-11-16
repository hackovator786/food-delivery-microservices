package com.hackovation.search_service.service;

import com.hackovation.search_service.model.MenuItemDocument;
import com.hackovation.search_service.model.RestaurantDocument;
import com.hackovation.search_service.repository.RestaurantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.StreamSupport;

@Service
public class RestaurantService {

    @Autowired
    private RestaurantRepository restaurantRepository;


    public List<RestaurantDocument> getAllRestaurants() throws IOException {

        return StreamSupport.stream(
                        restaurantRepository.findAll().spliterator(), false)
                .toList();
    }
}
