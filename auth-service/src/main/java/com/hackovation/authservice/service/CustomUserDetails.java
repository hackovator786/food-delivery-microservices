package com.hackovation.authservice.service;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Objects;


import com.hackovation.authservice.exception.ApiException;
import com.hackovation.authservice.exception.AuthException;
import com.hackovation.authservice.model.Role;
import com.hackovation.authservice.model.User;
import com.hackovation.authservice.model.UserRoleMapping;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@NoArgsConstructor
@Data
public class CustomUserDetails implements UserDetails {
    private static final long serialVersionUID = 1L;

    private String userId;
    private String email;

    private Boolean accountNonLocked;

    private Integer failedAttempts;
    private Long lockedAt;

    private Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(String userId, String email, Boolean accountNonLocked,
                             Integer failedAttempts, Long lockedAt,
                             Collection<? extends GrantedAuthority> authorities) {
        this.userId = userId;
        this.email = email;
        this.accountNonLocked = accountNonLocked;
        this.failedAttempts = failedAttempts;
        this.lockedAt = lockedAt;
        this.authorities = authorities;
    }

    public static CustomUserDetails build(User user, Role role) throws AuthException {
        Collection<? extends GrantedAuthority> authorities =
                user.getRoles()
                        .stream()
                        .map(r -> new SimpleGrantedAuthority(r.getRoleId().toString()))
                        .toList();

        UserRoleMapping roleMapping = user.getUserRoleMapping(role);
        if( Objects.isNull(roleMapping) )
            throw new AuthException("User does not have the required role", HttpStatus.BAD_REQUEST);

        return new CustomUserDetails(
                user.getUserId(),
                user.getEmail(),
                roleMapping.getAccountNonLocked(),
                roleMapping.getFailedAttempts(),
                roleMapping.getLockedAt(),
                authorities
        );
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        if( lockedAt == null ) {
            return Boolean.TRUE.equals(accountNonLocked);
        }
        return Boolean.FALSE.equals(accountNonLocked) && lockedAt + 1000L * 60 * 2 <= Instant.now().toEpochMilli();
    }

    @Override public String getPassword() { return null; }
    @Override public String getUsername() { return email; }
    @Override public boolean isAccountNonExpired() { return true; }
}
