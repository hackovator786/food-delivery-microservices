package com.hackovation.menu_service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(
        name = "menu_category",
        uniqueConstraints = {
                @UniqueConstraint(name = "menu_category_unique", columnNames = {"restaurant_id", "name"})
        }
)
public class MenuCategory extends BaseModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String restaurantId;
    @NotBlank
    @Size(min = 2, max = 20, message = "Name must be between 2 and 20 characters")
    private String name;

    private Integer sortOrder;

    @OneToMany(mappedBy = "category", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<MenuItem> menuItems;

    @PrePersist
    public void prePersist() {
        super.prePersist();
    }

    @PreUpdate
    public void preUpdate() {
        super.preUpdate();
    }
}
