package com.hackovation.authservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;


@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "user_roles")
public class UserRoleMapping extends BaseModel implements Serializable {

    @EmbeddedId
    private UserRoleId id = new UserRoleId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    @ToString.Exclude
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("roleId")
    @JoinColumn(name = "role_id")
    @ToString.Exclude
    private Role role;

    @Column(name = "assigned_at")
    private Long assignedAt = System.currentTimeMillis();

    @Column(name = "granted_by")
    private String grantedBy = "System";

    @NotNull
    private Boolean accountNonLocked = true;

    @NotNull
    private Integer failedAttempts = 0;

    private Long lockedAt;

    private String refreshToken;

    private Long lastLoginAt;


    public UserRoleMapping(User user, Role role) {
        this.user = user;
        this.role = role;
        this.id = new UserRoleId(user.getId(), role.getId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserRoleMapping that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
