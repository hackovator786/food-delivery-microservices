package com.hackovation.userservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.HashSet;
import java.util.Set;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "user_id_unique", columnNames = "user_id"),
                @UniqueConstraint(name = "email_unique", columnNames = "email")
        },
        indexes = {
                @Index(name = "user_id_index", columnList = "user_id"),
                @Index(name = "email_index", columnList = "email")
        }
)
public class User extends BaseModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String userId;

    @NotBlank
    @Pattern(regexp = "^[a-zA-Z ]{1,40}$", message = "Invalid name")
    private String name;

    @NotBlank
    @Size(min = 4, max = 50, message = "Invalid email address")
    @Email(message = "Invalid email address")
    private String email;

    private Long phoneNumber;

    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private Set<Address> addresses = new HashSet<>();

    public void addAddress(Address address) {
        addresses.add(address);
        address.setUser(this);
    }

    public void removeAddress(Address address) {
        addresses.remove(address);
        address.setUser(null);
    }

    @PrePersist
    public void prePersist() {
        super.prePersist();
    }

    @PreUpdate
    public void preUpdate() {
        super.preUpdate();
    }
}
