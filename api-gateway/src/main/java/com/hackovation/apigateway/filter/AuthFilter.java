package com.hackovation.apigateway.filter;

import com.hackovation.apigateway.util.SecureJwtUtils;
import com.nimbusds.jwt.JWTClaimsSet;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;


@Component
public class AuthFilter extends AbstractGatewayFilterFactory<AuthFilter.Config> {

    @Autowired
    private SecureJwtUtils jwtUtil;

    @Value("${spring.app.role.role_customer}")
    private Integer ROLE_CUSTOMER;
    @Value("${spring.app.role.role_admin}")
    private Integer ROLE_ADMIN;
    @Value("${spring.app.role.role_restaurant_owner}")
    private Integer ROLE_RESTAURANT_OWNER;
    @Value("${spring.app.role.role_delivery_agent}")
    private Integer ROLE_DELIVERY_AGENT;

    public AuthFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            Map<Integer, String> roles = Map.of(
                    ROLE_CUSTOMER, "ROLE_CUSTOMER",
                    ROLE_ADMIN, "ROLE_ADMIN",
                    ROLE_RESTAURANT_OWNER, "ROLE_RESTAURANT_OWNER",
                    ROLE_DELIVERY_AGENT, "ROLE_DELIVERY_AGENT"
            );
            ServerHttpRequest request = null;
            try {
                System.out.println("Roles list: " + config.getRoles());
                //header contains token or not
                if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                    throw new RuntimeException("missing authorization header");
                }

                String authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    authHeader = authHeader.substring(7);
                }
                String userId;
                Integer userRoleId;
                try {
                    System.out.println("Auth Header: " + authHeader);
                    JWTClaimsSet claimsSet = jwtUtil.validateJwtToken(authHeader);
                    userId = claimsSet.getSubject();
                    System.out.println("User ID: " + userId);
                    userRoleId = claimsSet.getIntegerClaim("roleId");
                    System.out.println("User ID: " + userId);
                    System.out.println("User Role Id: " + userRoleId);
                    if(userId == null || userId.isBlank() || userRoleId == null)
                        throw new RuntimeException("Invalid access token");
                } catch (Exception e) {
                    System.out.println("Invalid access token");
                    throw new RuntimeException("Invalid access token");
                }

                if(!config.getRoles().isEmpty() && !config.getRoles().contains(roles.getOrDefault(userRoleId, "NONE"))){
                    throw new RuntimeException("You are not authorized to access this resource");
                }
            } catch (RuntimeException e) {
                return handleException(exchange, e.getMessage(), HttpStatus.UNAUTHORIZED);
            } catch (Exception e) {
                e.printStackTrace();
                return handleException(exchange, "Unknown Error has occurred", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            return chain.filter(exchange.mutate().request(request).build());
        });
    }

    private Mono<Void> handleException(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);


        String errorBody = "{\"error\": \"" + message + "\"}";
        byte[] bytes = errorBody.getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bytes);

        return response.writeWith(Mono.just(buffer));
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Config {
        private List<String> roles;
    }
}
