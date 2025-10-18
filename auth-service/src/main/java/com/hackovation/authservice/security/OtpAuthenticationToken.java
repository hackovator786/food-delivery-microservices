package com.hackovation.authservice.security;

import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;

@Getter
public class OtpAuthenticationToken extends AbstractAuthenticationToken {

    private final String email;
    private final String otp;
    private final String role;

    public OtpAuthenticationToken(String email, String otp, String role) {
        super(null);
        this.email = email;
        this.otp = otp;
        this.role = role;
        setAuthenticated(false);
    }

    @Override
    public Object getCredentials() {
        return otp;
    }

    @Override
    public Object getPrincipal() {
        return email;
    }

    @Override
    public Object getDetails() {
        return role;
    }
}
