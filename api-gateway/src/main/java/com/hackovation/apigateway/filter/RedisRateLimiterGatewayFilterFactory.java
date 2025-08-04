package com.hackovation.apigateway.filter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.Objects;

@Component
public class RedisRateLimiterGatewayFilterFactory extends AbstractGatewayFilterFactory<RedisRateLimiterGatewayFilterFactory.Config> {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public RedisRateLimiterGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            try {
                String clientIp = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
                if (clientIp == null) {
                    clientIp = Objects.requireNonNull(exchange.getRequest().getRemoteAddress()).getAddress().getHostAddress();
                }

                String key = "rate_limit:" + config.getRouteId() + ":" + clientIp;
                Long count = redisTemplate.opsForValue().increment(key);

                if (count == 1) {
                    redisTemplate.expire(key, Duration.ofSeconds(config.getDurationInSeconds()));
                }

                if (count > config.getMaxRequests()) {
                    throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Rate limit exceeded");
                }

                return chain.filter(exchange);
            } catch (ResponseStatusException e) {
                throw e;
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
            }
        };
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Config {
        private String routeId;
        private int maxRequests;
        private int durationInSeconds;
    }
}
