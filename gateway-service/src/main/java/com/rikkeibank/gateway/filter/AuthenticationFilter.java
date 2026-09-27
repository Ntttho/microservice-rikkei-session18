package com.rikkeibank.gateway.filter;

import com.rikkeibank.gateway.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
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

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired(required = false)
    private ReactiveStringRedisTemplate redisTemplate;

    private static final List<String> OPEN_API_ENDPOINTS = List.of(
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/refresh",
            "/eureka",
            "/actuator/health"
    );

    public AuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath();

            if (isSecured(path)) {
                if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                    return onError(exchange, "Missing Authorization Header", HttpStatus.UNAUTHORIZED);
                }

                String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    return onError(exchange, "Invalid Authorization Header format. Expected Bearer <token>", HttpStatus.UNAUTHORIZED);
                }

                String token = authHeader.substring(7);
                if (!jwtUtil.validateToken(token)) {
                    return onError(exchange, "Invalid or Expired JWT Token", HttpStatus.UNAUTHORIZED);
                }

                try {
                    Claims claims = jwtUtil.extractAllClaims(token);
                    String email = claims.getSubject();
                    String roles = claims.get("roles", String.class);
                    Object userId = claims.get("userId");

                    // Check token blacklist in Redis if available
                    if (redisTemplate != null) {
                        return redisTemplate.hasKey("blacklist:" + token)
                                .flatMap(isBlacklisted -> {
                                    if (Boolean.TRUE.equals(isBlacklisted)) {
                                        return onError(exchange, "Token has been revoked/logged out by administrator", HttpStatus.UNAUTHORIZED);
                                    }
                                    ServerHttpRequest modifiedRequest = request.mutate()
                                            .header("X-User-Email", email)
                                            .header("X-User-Roles", roles != null ? roles : "")
                                            .header("X-User-Id", userId != null ? String.valueOf(userId) : "")
                                            .build();
                                    return chain.filter(exchange.mutate().request(modifiedRequest).build());
                                })
                                .onErrorResume(e -> {
                                    // Fallback if Redis is unreachable
                                    ServerHttpRequest modifiedRequest = request.mutate()
                                            .header("X-User-Email", email)
                                            .header("X-User-Roles", roles != null ? roles : "")
                                            .header("X-User-Id", userId != null ? String.valueOf(userId) : "")
                                            .build();
                                    return chain.filter(exchange.mutate().request(modifiedRequest).build());
                                });
                    }

                    ServerHttpRequest modifiedRequest = request.mutate()
                            .header("X-User-Email", email)
                            .header("X-User-Roles", roles != null ? roles : "")
                            .header("X-User-Id", userId != null ? String.valueOf(userId) : "")
                            .build();

                    return chain.filter(exchange.mutate().request(modifiedRequest).build());
                } catch (Exception e) {
                    return onError(exchange, "Failed to authenticate token: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
                }
            }
            return chain.filter(exchange);
        };
    }

    private boolean isSecured(String path) {
        return OPEN_API_ENDPOINTS.stream().noneMatch(path::startsWith);
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String body = String.format("{\"timestamp\":\"%s\",\"status\":%d,\"error\":\"%s\",\"message\":\"%s\"}",
                java.time.Instant.now().toString(),
                httpStatus.value(),
                httpStatus.getReasonPhrase(),
                err);

        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    public static class Config {
    }
}
