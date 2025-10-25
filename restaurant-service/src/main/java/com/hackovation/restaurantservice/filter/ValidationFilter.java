package com.hackovation.restaurantservice.filter;

import com.hackovation.restaurantservice.exception.AuthFilterException;
import com.hackovation.restaurantservice.repository.RestaurantRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

@Component
public class ValidationFilter extends OncePerRequestFilter {

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException, AuthFilterException {
        Set<String> exceptionUris = Set.of("/restaurant/create");
        try {
            if(!request.getRequestURI().startsWith(contextPath + "/internal") && !exceptionUris.contains(request.getRequestURI().replace(contextPath, ""))){
                String restaurantId = request.getHeader("restaurantId");
                System.out.println("Inside Validation filter --> Restaurant Id: " + restaurantId);
                if(restaurantId == null || restaurantId.isBlank())
                    throw new AuthFilterException("Invalid restaurant");
                String userId = request.getHeader("loggedInUser");
                if(!restaurantRepository.existsByRestaurantIdAndOwnerId(restaurantId,userId))
                    throw new AuthFilterException("Invalid user or restaurant. Please try again with valid credentials.");
            }
            filterChain.doFilter(request, response);
        } catch (AuthFilterException e) {
            request.setAttribute(AuthFilterException.CUSTOM_AUTH_ERROR_MESSAGE, e.getMessage());
            throw e;
        } catch (Exception e) {
            request.setAttribute(AuthFilterException.CUSTOM_AUTH_ERROR_MESSAGE, "Authentication failed");
            throw new AuthFilterException("Authentication failed", e);
        }
    }
}
