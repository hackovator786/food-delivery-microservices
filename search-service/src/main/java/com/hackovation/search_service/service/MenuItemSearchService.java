package com.hackovation.search_service.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.hackovation.search_service.dto.MenuItemResponse;
import com.hackovation.search_service.dto.SearchRequestDto;
import com.hackovation.search_service.model.MenuItemDocument;
import com.hackovation.search_service.repository.MenuItemSearchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class MenuItemSearchService {

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    @Autowired
    private MenuItemSearchRepository menuItemSearchRepository;

    public Page<MenuItemResponse> searchMenuItems(SearchRequestDto dto, Pageable pageable) throws IOException {
        int from = (int) pageable.getOffset();
        int size = pageable.getPageSize();

        List<Query> mustQueries = new ArrayList<>();

        // Filter by menuItemId
        if (dto.getMenuItemId() != null && !dto.getMenuItemId().isEmpty()) {
            mustQueries.add(
                    TermQuery.of(t -> t
                            .field("menuItemId")
                            .value(dto.getMenuItemId())
                    )._toQuery());
        }

        // Keyword search on name or description
        if (dto.getQuery() != null && !dto.getQuery().isEmpty()) {
            mustQueries.add(
                    MultiMatchQuery.of(m -> m
                    .fields("menuItemName", "description", "restaurantName" , "category")
                    .query(dto.getQuery())
            )._toQuery());
        }

        // Filter by restaurantId
        if (dto.getRestaurantId() != null && !dto.getRestaurantId().isEmpty()) {
            mustQueries.add(
                    TermQuery.of(t -> t
                    .field("restaurantId")
                    .value(dto.getRestaurantId())
            )._toQuery());
        }

        // Tags filter - nested query on tags.tagName.keyword
        if (dto.getTags() != null && !dto.getTags().isEmpty()) {
            for (String tag : dto.getTags()) {
                mustQueries.add(
                        NestedQuery.of(n -> n
                                .path("tags")
                                .query(q -> q.term(t -> t
                                        .field("tags.tagName.keyword")
                                        .value(tag)
                                ))
                        )._toQuery()
                );
            }
        }

        // Filter by category
        if (dto.getCategory() != null && !dto.getCategory().isEmpty()) {
            mustQueries.add(
                    TermQuery.of(t -> t
                            .field("category")
                            .value(dto.getCategory())
                    )._toQuery());
        }

        // Price range
        if (dto.getMinPrice() != null || dto.getMaxPrice() != null) {
            mustQueries.add(RangeQuery.of(q -> q.number(r -> {
                r.field("price");
                if (dto.getMinPrice() != null) {
                    r.gte(dto.getMinPrice());
                }
                if (dto.getMaxPrice() != null) {
                    r.lte(dto.getMaxPrice());
                }
                return r;
            }))._toQuery());
        }


        // Availability
        if (dto.getIsAvailable() != null) {
            mustQueries.add(
                    TermQuery.of(t -> t
                    .field("isAvailable")
                    .value(dto.getIsAvailable())
            )._toQuery());
        }

        // Build bool query
        BoolQuery boolQuery = BoolQuery.of(b -> b.must(mustQueries));

        SearchRequest searchRequest = SearchRequest.of(s -> s
                .index("menu-items")
                .from(from)
                .size(size)
                .query(boolQuery._toQuery())
        );

        SearchResponse<MenuItemDocument> response = elasticsearchClient.search(searchRequest, MenuItemDocument.class);

        List<MenuItemResponse> menuItems = response.hits().hits().stream()
                .map(hit -> hit.source())
                .collect(Collectors.toList()).stream().map(this::convertToMenuItemResponse).collect(Collectors.toList());

        long totalHits = response.hits().total() != null ? response.hits().total().value() : 0;

        return new PageImpl<>(menuItems, pageable, totalHits);
    }

    public List<MenuItemResponse> getAllMenuItems() throws IOException {
        return StreamSupport.stream(
                        menuItemSearchRepository.findAll().spliterator(), false)
                .toList().stream().map(this::convertToMenuItemResponse).collect(Collectors.toList());
    }

    private MenuItemResponse convertToMenuItemResponse(MenuItemDocument doc) {
        return MenuItemResponse.builder()
                .menuItemId(doc.getMenuItemId())
                .restaurantId(doc.getRestaurantId())
                .menuItemName(doc.getMenuItemName())
                .description(doc.getDescription())
                .price(doc.getPrice())
                .isAvailable(doc.getIsAvailable())
                .imageUrl(doc.getImageUrl())
                .build();
    }
}

