package com.hackovation.orderservice.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, foreignKey = @ForeignKey(name = "fk_order_item_order"))
    private Order order;

    @NotBlank(message = "Menu Item Id cannot be blank")
    private String menuItemId;

    @NotBlank(message = "Item Name cannot be blank")
    private String itemName;

    @NotNull(message = "Quantity cannot be null")
    private Integer quantity;

    @NotNull(message = "Unit Price cannot be null")
    private Double unitPriceSnapshot;

    private String notes;
}
