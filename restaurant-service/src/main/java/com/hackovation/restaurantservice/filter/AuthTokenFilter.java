package com.hackovation.restaurantservice.filter;

import com.hackovation.restaurantservice.exception.AuthFilterException;
import com.hackovation.restaurantservice.utils.AccessTokenUtils;
import com.nimbusds.jwt.JWTClaimsSet;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.*;

@Component
public class AuthTokenFilter extends OncePerRequestFilter {
    @Autowired
    private AccessTokenUtils accessTokenUtils;

    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

    @Value("${spring.app.role.role_customer}")
    private Integer ROLE_CUSTOMER;
    @Value("${spring.app.role.role_admin}")
    private Integer ROLE_ADMIN;
    @Value("${spring.app.role.role_restaurant_owner}")
    private Integer ROLE_RESTAURANT_OWNER;
    @Value("${spring.app.role.role_delivery_agent}")
    private Integer ROLE_DELIVERY_AGENT;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException, AuthFilterException {
        HttpServletRequest wrappedRequest = request;
                logger.debug("AuthTokenFilter called for URI: {}", request.getRequestURI());
        Map<Integer, String> roles = Map.of(
                ROLE_CUSTOMER, "ROLE_CUSTOMER",
                ROLE_ADMIN, "ROLE_ADMIN",
                ROLE_RESTAURANT_OWNER, "ROLE_RESTAURANT_OWNER",
                ROLE_DELIVERY_AGENT, "ROLE_DELIVERY_AGENT"
        );
        try {
            String jwt = parseJwt(request);
            if (jwt != null) {
                JWTClaimsSet claimsSet = accessTokenUtils.validateAccessToken(jwt);
                String userId = claimsSet.getSubject();
                Integer userRoleId = claimsSet.getIntegerClaim("roleId");
                List<String> rolesList = List.of(roles.getOrDefault(userRoleId, "NONE"));
                System.out.println("User ID: --> " + userId);
                System.out.println("User Role ID: --->" + userRoleId);
                System.out.println("User Roles: ---->" + rolesList);

                List<SimpleGrantedAuthority> authorities = rolesList
                        .stream()
                        .map(SimpleGrantedAuthority::new)
                        .toList();

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userId, null, authorities);

                Map<String, String> customHeaders = new HashMap<>();
                customHeaders.put("loggedInUser", userId);

                wrappedRequest = new CustomHeaderRequestWrapper(request, customHeaders);

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(wrappedRequest));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            filterChain.doFilter(wrappedRequest, response);
            logger.debug("AuthTokenFilter finished for URI: {}", request.getRequestURI());
        } catch (AuthFilterException e) {
            request.setAttribute(AuthFilterException.CUSTOM_AUTH_ERROR_MESSAGE, e.getMessage());
            throw e;
        } catch (Exception e) {
            request.setAttribute(AuthFilterException.CUSTOM_AUTH_ERROR_MESSAGE, "Authentication failed");
            throw new AuthFilterException("Authentication failed", e);
        }

    }

    private String parseJwt(HttpServletRequest request) {
        String jwt = accessTokenUtils.getAccessTokenFromHeader(request);
        logger.debug("AuthTokenFilter.java: {}", jwt);
        return jwt;
    }


    /**
     * Custom request wrapper to inject multiple headers.
     */
    private static class CustomHeaderRequestWrapper extends HttpServletRequestWrapper {

        private final Map<String, String> customHeaders;

        public CustomHeaderRequestWrapper(HttpServletRequest request, Map<String, String> customHeaders) {
            super(request);
            this.customHeaders = new HashMap<>(customHeaders);
        }

        @Override
        public String getHeader(String name) {
            String headerValue = customHeaders.get(name);
            if (headerValue != null) {
                return headerValue;
            }
            return super.getHeader(name);
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            Set<String> names = new HashSet<>(customHeaders.keySet());
            Enumeration<String> originalNames = super.getHeaderNames();
            while (originalNames.hasMoreElements()) {
                names.add(originalNames.nextElement());
            }
            return Collections.enumeration(names);
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            if (customHeaders.containsKey(name)) {
                return Collections.enumeration(List.of(customHeaders.get(name)));
            }
            return super.getHeaders(name);
        }
    }
}