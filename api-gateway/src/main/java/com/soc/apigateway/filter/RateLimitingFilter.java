package com.soc.apigateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitingFilter implements GlobalFilter, Ordered {

    private static final int MAX_REQUESTS_PER_MINUTE = 60;
    private final Map<String, RequestCounter> requestCounts = new ConcurrentHashMap<>();

    private static class RequestCounter {
        private final long startTime;
        private final AtomicInteger count;

        public RequestCounter(long startTime) {
            this.startTime = startTime;
            this.count = new AtomicInteger(1);
        }
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String clientIp = getClientIp(exchange);
        long now = System.currentTimeMillis();

        requestCounts.entrySet().removeIf(entry -> now - entry.getValue().startTime > 60000);

        RequestCounter counter = requestCounts.compute(clientIp, (ip, existingCounter) -> {
            if (existingCounter == null || now - existingCounter.startTime > 60000) {
                return new RequestCounter(now);
            }
            existingCounter.count.incrementAndGet();
            return existingCounter;
        });

        if (counter.count.get() > MAX_REQUESTS_PER_MINUTE) {
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

            String jsonError = String.format(
                    "{\"error\": \"Too Many Requests\", \"message\": \"Rate limit of %d requests per minute exceeded.\", \"status\": 429}",
                    MAX_REQUESTS_PER_MINUTE
            );
            byte[] bytes = jsonError.getBytes(StandardCharsets.UTF_8);

            return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
        }

        return chain.filter(exchange);
    }

    private String getClientIp(ServerWebExchange exchange) {
        String ip = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            if (exchange.getRequest().getRemoteAddress() != null) {
                ip = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
            } else {
                ip = "unknown";
            }
        }
        return ip;
    }

    @Override
    public int getOrder() {
        return -3;
    }
}
