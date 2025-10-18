package com.hackovation.authservice.service;

import com.hackovation.authservice.exception.AuthException;
import com.hackovation.authservice.model.Role;
import com.hackovation.authservice.model.User;
import com.hackovation.authservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class CustomUserDetailsService implements CustomUserDetailsServiceInterface {
    @Autowired
    UserRepository userRepository;

    @Override
    public CustomUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return null;
    }

    @Transactional
    @Override
    public CustomUserDetails loadUserByUsernameAndRole(String userId, Role role) throws UsernameNotFoundException, AuthException {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        if(!user.getRoles().contains(role)) throw new AuthException("User does not have the required role", HttpStatus.BAD_REQUEST);
        return CustomUserDetails.build(user, role);
    }

}
