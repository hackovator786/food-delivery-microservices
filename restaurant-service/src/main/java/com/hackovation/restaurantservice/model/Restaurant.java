package com.hackovation.restaurantservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
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
        name = "restaurant",
        uniqueConstraints = {
                @UniqueConstraint(name = "restaurant_id_owner_id_unique", columnNames = {"restaurant_id", "owner_id"}),
                @UniqueConstraint(name = "email_unique", columnNames = {"contact_email"})
        }
)
public class Restaurant extends BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String restaurantId;

    @NotBlank
    private String ownerId; // userId

    @NotBlank
    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    private RestaurantStatus status;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "address_id")
    private Address address;

    private String cuisine;

    private String contactEmail;

    private List<Long> contactNumbers;
    private Double avgRating;

    @PrePersist
    public void prePersist() {
        super.prePersist();
        this.status = RestaurantStatus.OPEN;
    }

    @PreUpdate
    public void preUpdate() {
        super.preUpdate();
    }
}
