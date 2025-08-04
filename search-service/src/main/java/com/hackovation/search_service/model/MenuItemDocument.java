package com.hackovation.search_service.model;
import java.time.Instant;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(indexName = "menu-items", createIndex = true)
public class MenuItemDocument {

    @Id
    @Field(type = FieldType.Keyword)
    private String menuItemId;

    @Field(type = FieldType.Keyword)
    private String  restaurantId;

    @Field(type = FieldType.Keyword)
    private String  restaurantName;

    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "standard", fielddata = true),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword)
            }
    )
    private String menuItemName;

    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "standard", fielddata = true),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword)
            }
    )
    private String description;

    @Field(type = FieldType.Keyword)
    private String category;

    @Field(type = FieldType.Double)
    private Double price;

    @Field(type = FieldType.Boolean)
    private Boolean isAvailable;

    @Field(type = FieldType.Nested, includeInParent = true)
    private Set<TagDoc> tags;

    @Field(type = FieldType.Keyword)
    private String imageUrl;

    @Field(type = FieldType.Date)
    private Instant createdAt;

    @Field(type = FieldType.Date)
    private Instant updatedAt;

    // Nested static class
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TagDoc {
        @MultiField(
                mainField = @Field(type = FieldType.Text),
                otherFields = {
                        @InnerField(suffix = "keyword", type = FieldType.Keyword)
                }
        )
        private String tagName;
    }
}


