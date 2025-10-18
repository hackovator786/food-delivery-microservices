package com.hackovation.authservice.service;

import com.hackovation.authservice.exception.AuthException;
import com.hackovation.authservice.model.Role;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;

public interface CustomUserDetailsServiceInterface extends UserDetailsService {
    @Transactional
    CustomUserDetails loadUserByUsernameAndRole(String userId, Role role) throws UsernameNotFoundException, AuthException;
}
