package com.hackovation.search_service.repository;

import com.hackovation.search_service.model.MenuItemDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import java.util.List;

public interface MenuItemSearchRepository
        extends ElasticsearchRepository<MenuItemDocument, String> {

    List<MenuItemDocument> findByRestaurantId(String restaurantId);
}
