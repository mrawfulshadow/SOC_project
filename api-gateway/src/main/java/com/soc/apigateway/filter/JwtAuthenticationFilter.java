package com.soc.apigateway.filter;

import com.soc.apigateway.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${api-keys.product-service:PRODUCT-SERVICE-SECRET-KEY}")
    private String apiKeyProduct;

    @Value("${api-keys.order-service:order-secret-key-123}")
    private String apiKeyOrder;

    @Value("${api-keys.payment-service:payment-secret-key-123}")
    private String apiKeyPayment;

    @Value("${api-keys.notification-service:notification-secret-key-123}")
    private String apiKeyNotification;

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private static final List<String> OPEN_ENDPOINTS = List.of(
            "/api/auth/register",
            "/api/auth/login",
            "/api/auth/validate",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/v3/api-docs",
            "/api-docs/**",
            "/api-docs"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String rawPath = request.getURI().getRawPath();
        String path = request.getURI().getPath();

        if (isInvalidPath(rawPath, path)) {
            return onError(exchange, "Access Denied: Invalid path traversal or forbidden characters detected", HttpStatus.BAD_REQUEST);
        }

        if (isOpenEndpoint(path)) {
            return chain.filter(exchange);
        }

        if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
            return onError(exchange, "Missing Authorization Header", HttpStatus.UNAUTHORIZED);
        }

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return onError(exchange, "Invalid Authorization Header Format", HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7);

        if (!jwtUtil.validateToken(token)) {
            return onError(exchange, "Invalid or Expired JWT Token", HttpStatus.UNAUTHORIZED);
        }

        try {
            Claims claims = jwtUtil.getClaimsFromToken(token);
            String username = claims.getSubject();
            String role = claims.get("role", String.class);

            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-User-Name", username)
                    .header("X-User-Role", role != null ? role : "ROLE_USER")
                    .build();

            return chain.filter(exchange.mutate().request(modifiedRequest).build());

        } catch (Exception e) {
            return onError(exchange, "Invalid or Expired JWT Token", HttpStatus.UNAUTHORIZED);
        }
    }

    private boolean isInvalidPath(String rawPath, String path) {
        if (rawPath == null || path == null) {
            return true;
        }
        String lowerRawPath = rawPath.toLowerCase();
        return rawPath.contains("..")
                || path.contains("..")
                || lowerRawPath.contains("%2e%2e")
                || rawPath.contains(";")
                || path.contains(";")
                || lowerRawPath.contains("%2f")
                || lowerRawPath.contains("%5c")
                || path.contains("\\");
    }

    private boolean isOpenEndpoint(String path) {
        if (path == null) {
            return false;
        }
        String normalizedPath = path.endsWith("/") && path.length() > 1
                ? path.substring(0, path.length() - 1)
                : path;

        return OPEN_ENDPOINTS.stream()
                .anyMatch(pattern -> PATH_MATCHER.match(pattern, path) || PATH_MATCHER.match(pattern, normalizedPath));
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String jsonError = String.format("{\"error\": \"%s\", \"status\": %d}", err, httpStatus.value());
        byte[] bytes = jsonError.getBytes(StandardCharsets.UTF_8);

        return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
    }

    @Override
    public int getOrder() {
        return -2;
    }
}
