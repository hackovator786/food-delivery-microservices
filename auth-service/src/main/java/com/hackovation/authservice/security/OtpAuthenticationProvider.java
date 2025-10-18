package com.hackovation.authservice.security;

import com.hackovation.authservice.enums.RequestType;
import com.hackovation.authservice.enums.UserRole;
import com.hackovation.authservice.exception.AuthException;
import com.hackovation.authservice.model.Role;
import com.hackovation.authservice.model.User;
import com.hackovation.authservice.repository.RoleRepository;
import com.hackovation.authservice.repository.UserRepository;
import com.hackovation.authservice.service.CustomUserDetailsService;
import com.hackovation.authservice.service.OtpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class OtpAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private OtpService otpService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String email = authentication.getPrincipal().toString();
        String otp = authentication.getCredentials().toString();
        String role = authentication.getDetails().toString();

        Role returnedRole =  roleRepository.findByRoleName(UserRole.valueOf(role.toUpperCase()))
                .orElseThrow(() -> new BadCredentialsException("Invalid role"));

        User user = userRepository.findByEmail(email).orElseThrow(() ->
                new BadCredentialsException("Email does not exist\nPlease signup or enter a valid email"));

        if(!user.getRoles().contains(returnedRole)) {
            throw new BadCredentialsException("Invalid role");
        }

        if (!otpService.validateOtp(RequestType.LOGIN.name().toUpperCase(), email, otp,role)) {
            throw new BadCredentialsException("Invalid OTP or the OTP has expired\nPlease enter a valid OTP or request a new OTP");
        }

        UserDetails userDetails = null;
        try {
            userDetails = customUserDetailsService.loadUserByUsernameAndRole(user.getUserId(),returnedRole);
            if (userDetails == null) {
                throw new BadCredentialsException("User not found");
            }
            if (!userDetails.isEnabled()) {
                throw new DisabledException("User is disabled");
            }
            if (!userDetails.isAccountNonLocked()) {
                throw new LockedException("User account is locked");
            }
        } catch (BadCredentialsException | DisabledException | LockedException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e.getCause());
        }

        return new UsernamePasswordAuthenticationToken(
            userDetails, null, userDetails.getAuthorities()
        );
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return OtpAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
