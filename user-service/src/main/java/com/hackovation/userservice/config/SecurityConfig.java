package com.hackovation.userservice.config;


import com.hackovation.userservice.filter.AuthTokenFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true,
        securedEnabled = true,
        jsr250Enabled = true)
public class SecurityConfig {
    @Autowired
    private CustomAuthEntryPoint unauthorizedHandler;

    @Autowired
    private AuthTokenFilter authTokenFilter;

    @Bean
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable);
        http.authorizeHttpRequests((requests)
                -> requests
                .requestMatchers("/user/**").hasAnyRole("CUSTOMER", "ADMIN", "RESTAURANT_OWNER", "DELIVERY_AGENT")
                .requestMatchers("/internal/**").permitAll()
                .anyRequest().denyAll());
        http.exceptionHandling(exception
                -> exception.authenticationEntryPoint(unauthorizedHandler));
        http.addFilterBefore(authTokenFilter,
                UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

}