package com.hackovation.authservice.model;

import com.hackovation.authservice.enums.UserRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

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
    @Pattern(regexp = "^[a-zA-Z ]{1,40}$", message = "Invalid name")
    private String name;

    @NotBlank
    @Size(min = 4, max = 50, message = "Invalid email address")
    @Email(message = "Invalid email address")
    private String email;

    private Long phoneNumber;

    @NotNull
    private Boolean accountNonLocked = true;
    @NotNull
    private Integer failedAttempts = 0;
    private Long lockedAt;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    private String refreshToken;


    public User(String userId, String name, String email, UserRole role) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
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