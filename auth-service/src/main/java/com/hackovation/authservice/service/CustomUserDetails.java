package com.hackovation.authservice.service;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Objects;


import com.hackovation.authservice.model.User;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;

@NoArgsConstructor
@Data
public class CustomUserDetails implements UserDetails {
    private static final long serialVersionUID = 1L;

    private String userId;
    private String email; // username

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

    public static CustomUserDetails build(User user) {
        GrantedAuthority authority = new SimpleGrantedAuthority(user.getRole().name());

        return new CustomUserDetails(
                user.getUserId(),
                user.getEmail(),
                user.getAccountNonLocked(),
                user.getFailedAttempts(),
                user.getLockedAt(),
                List.of(authority)
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

    // mandatory implementations
    @Override public String getPassword() { return null; }
    @Override public String getUsername() { return email; }
    @Override public boolean isAccountNonExpired() { return true; }
}
