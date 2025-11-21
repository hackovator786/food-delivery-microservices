package com.hackovation.cartservice.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {

    @NotBlank
    @Field("menu_item_id")
    private String menuItemId;

    @NotBlank
    @Field("name")
    private String name;

    @Min(value = 1)
    @Field("quantity")
    private Integer quantity;

    @Min(value = 1)
    @Field("unit_price")
    private Double unitPrice;

    @Field("notes")
    private String notes;
}
