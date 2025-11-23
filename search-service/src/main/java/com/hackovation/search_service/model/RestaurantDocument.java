package com.hackovation.search_service.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(indexName = "restaurants", createIndex = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RestaurantDocument {

    @Id
    @JsonIgnore
    private String id;

    @Field(type = FieldType.Keyword)
    private String restaurantId;

    @Field(type = FieldType.Keyword)
    private String restaurantName;

    @Field(type = FieldType.Double)
    private Double rating;

    @Field(type = FieldType.Keyword)
    private String city;

    @Field(type = FieldType.Keyword)
    private String imageUrl;
}
