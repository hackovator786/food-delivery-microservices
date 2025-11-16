package com.hackovation.cartservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "cart_item")
public class CartItem extends BaseModel{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cartItemId;

    @NotBlank
    private String menuItemId;

    @Min(value = 1)
    private Integer quantity;

    private String notes;

    @ManyToOne
    @JoinColumn(name = "cart_id")
    private Cart cart;

    @PrePersist
    public void prePersist() {
        super.prePersist();
    }

    @PreUpdate
    public void preUpdate() {
        super.preUpdate();
    }
}
