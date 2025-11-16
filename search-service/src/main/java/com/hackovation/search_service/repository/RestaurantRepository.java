package com.hackovation.search_service.repository;

import com.hackovation.search_service.model.RestaurantDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RestaurantRepository extends ElasticsearchRepository<RestaurantDocument, String> {
    RestaurantDocument findByRestaurantId(String restaurantId);
}
