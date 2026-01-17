package com.hackovation.cartservice.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "cart")
public class Cart extends BaseModel{

    @Id
    @Field("user_id")
    private String userId;

    @NotBlank
    @Field("restaurant_id")
    private String restaurantId;

    @Min(value = 1)
    @Field(name = "sub_total", targetType = FieldType.DECIMAL128)
    private BigDecimal subTotal;

    @Field("items")
    private List<CartItem> items;

    @Field("notes")
    private String notes;

    public Cart(String userId, String restaurantId) {
        this.userId = userId;
        this.restaurantId = restaurantId;
    }

}
