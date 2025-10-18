package com.hackovation.authservice.model;

import com.hackovation.authservice.exception.AuthException;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(name = "users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "user_id", name = "uk_user_id"),
                @UniqueConstraint(columnNames = "email", name = "uk_email_id")
        },
        indexes = {
                @Index(name = "idx_user_id", columnList = "user_id"),
                @Index(name = "idx_email", columnList = "email")
        }
)
public class User extends BaseModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String userId;

    @NotBlank
    @Size(min = 4, max = 50, message = "Invalid email address")
    @Email(message = "Invalid email address")
    private String email;

    private Long phoneNumber;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserRoleMapping> userRoles = new HashSet<>();

    public User(String userId, String email) {
        this.userId = userId;
        this.email = email;
    }

    public Set<Role> getRoles() {
        Set<Role> roles = new HashSet<>();
        for (UserRoleMapping mapping : userRoles) {
            roles.add(mapping.getRole());
        }
        return roles;
    }

    public void addRole(Role role) {
        UserRoleMapping mapping = new UserRoleMapping(this, role);
        userRoles.add(mapping);
        role.getUserRoles().add(mapping);
    }

    public void removeRole(Role role) {
        userRoles.removeIf(mapping -> mapping.getRole().equals(role));
        role.getUserRoles().removeIf(mapping -> mapping.getUser().equals(this));
    }

    public UserRoleMapping getUserRoleMapping(Role role) {
        return getUserRoles().stream()
                .filter(
                        m -> Objects.equals(m.getRole().getId(), role.getId())
                )
                .findFirst()
                .orElse(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return id != null && id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode(); // Or Objects.hash(id) if id is assigned
    }

    public void validate() throws AuthException {
        if( Objects.isNull(userRoles) || userRoles.isEmpty() )
            throw new AuthException("User does not have any roles", HttpStatus.BAD_REQUEST);
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