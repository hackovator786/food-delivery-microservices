package com.hackovation.cartservice.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;

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
    private String itemName;

    @Min(value = 1)
    @Field("quantity")
    private Integer quantity;

    @Min(value = 1)
    @Field(name = "unit_price", targetType = FieldType.DECIMAL128)
    private BigDecimal unitPrice;
}
