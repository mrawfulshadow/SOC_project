package com.soc.apigateway.filter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class ApiKeyHeaderFilter implements GlobalFilter, Ordered {

    @Value("${api-keys.product-service:PRODUCT-SERVICE-SECRET-KEY}")
    private String productApiKey;

    @Value("${api-keys.payment-service:payment-secret-key-123}")
    private String paymentApiKey;

    @Value("${api-keys.notification-service:notification-secret-key-123}")
    private String notificationApiKey;

    @Value("${api-keys.order-service:order-secret-key-123}")
    private String orderApiKey;

    private static final String API_KEY_HEADER = "X-API-KEY";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        ServerHttpRequest.Builder builder = request.mutate();

        if (path.startsWith("/api/products") || path.startsWith("/products")) {
            builder.header(API_KEY_HEADER, productApiKey);
        } else if (path.startsWith("/api/payments")) {
            builder.header(API_KEY_HEADER, paymentApiKey);
        } else if (path.startsWith("/api/notifications")) {
            builder.header(API_KEY_HEADER, notificationApiKey);
        } else if (path.startsWith("/api/orders") || path.startsWith("/api/v1/orders")) {
            builder.header(API_KEY_HEADER, orderApiKey);
        }

        return chain.filter(exchange.mutate().request(builder.build()).build());
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
