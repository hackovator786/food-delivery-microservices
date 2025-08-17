package com.hackovation.authservice.filter;

import com.hackovation.authservice.exception.AuthFilterException;
import com.hackovation.authservice.service.CustomUserDetails;
import com.hackovation.authservice.service.CustomUserDetailsService;
import com.hackovation.authservice.util.AccessTokenUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class AuthTokenFilter extends OncePerRequestFilter {
    @Autowired
    private AccessTokenUtils accessTokenUtils;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException, AuthFilterException {
        logger.debug("AuthTokenFilter called for URI: {}", request.getRequestURI());
        try {
            String jwt = parseJwt(request);
            if (jwt != null) {
                String userId = accessTokenUtils.getUserIdFromAccessToken(jwt);

                System.out.println("User ID: --> " + userId);

                System.out.println("User Role: --->" + accessTokenUtils.getRoleFromAccessToken(jwt));

                CustomUserDetails userDetails = userDetailsService.loadUserByUsername(userId);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails,
                                null,
                                userDetails.getAuthorities());
                logger.debug("Roles from JWT: {}", userDetails.getAuthorities());

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            filterChain.doFilter(request, response);
            logger.debug("AuthTokenFilter finished for URI: {}", request.getRequestURI());
        } catch (AuthFilterException e) {
            logger.error("AuthFilterException: {}", e.getMessage());
            System.out.println("In AuthtokenFilter: AuthFilterException: " + e.getMessage());
            request.setAttribute(AuthFilterException.CUSTOM_AUTH_ERROR_MESSAGE, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error during authentication process", e);
            request.setAttribute(AuthFilterException.CUSTOM_AUTH_ERROR_MESSAGE, "Authentication failed");
            throw new AuthFilterException("Authentication failed", e);
        }

    }

    private String parseJwt(HttpServletRequest request) {
        String jwt = accessTokenUtils.getAccessTokenFromHeader(request);
        logger.debug("AuthTokenFilter.java: {}", jwt);
        return jwt;
    }
}