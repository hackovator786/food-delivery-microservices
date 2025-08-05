package com.hackovation.menu_service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;


@Data
@ToString(exclude = {"category", "tags"})
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(
        name = "menu_item",
        uniqueConstraints = {
                @UniqueConstraint(name = "unique_menu_item", columnNames = {"restaurant_id", "name"})
        }
)
public class MenuItem extends BaseModel {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank
    private String menuItemId;

    @NotBlank(message = "Restaurant ID is required")
    private String restaurantId;

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String menuItemName;

    @NotBlank(message = "Description is required")
    @Size(min = 2, max = 1000, message = "Description must be between 2 and 1000 characters")
    private String description;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    private MenuCategory category;

    @NotNull
    private Double price;

    @NotNull
    private Boolean isAvailable;

    @NotBlank(message = "Image URL is required")
    private String imageUrl;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "menu_item_tag",
            joinColumns = @JoinColumn(name = "menu_item_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private Set<Tag> tags = new HashSet<>();

    // Only include the 'id' in equals() and hashCode() to prevent the loop
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MenuItem menuItem = (MenuItem) o;
        return Objects.equals(id, menuItem.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @PrePersist
    public void prePersist() {
        super.prePersist();
        this.isAvailable = true; // Set isAvailable to true by default
    }

    @PreUpdate
    public void preUpdate() {
        super.preUpdate();
    }

}
