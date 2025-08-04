package com.hackovation.authservice.security;

import com.hackovation.authservice.enums.RequestType;
import com.hackovation.authservice.model.User;
import com.hackovation.authservice.repository.UserRepository;
import com.hackovation.authservice.service.CustomUserDetailsService;
import com.hackovation.authservice.service.OtpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String email = authentication.getPrincipal().toString();
        String otp = authentication.getCredentials().toString();

        User user = userRepository.findByEmail(email).orElseThrow(() ->
                new BadCredentialsException("Email does not exist\nPlease signup or enter a valid email"));

        if (!otpService.validateOtp(RequestType.LOGIN.name().toUpperCase(), email, otp)) {
            throw new BadCredentialsException("Invalid OTP or the OTP has expired\nPlease enter a valid OTP or request a new OTP");
        }

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(user.getUserId());

        return new UsernamePasswordAuthenticationToken(
            userDetails, null, userDetails.getAuthorities()
        );
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return OtpAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
