package com.soc.apigateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Component
public class RateLimitingFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(RateLimitingFilter.class);

    @Value("${gateway.rate-limit.requests-per-minute:60}")
    private int maxRequestsPerMinute;

    @Value("${gateway.rate-limit.window-seconds:60}")
    private int windowSeconds;

    @Value("${gateway.trusted-proxies:127.0.0.1,::1,10.0.0.0/8,172.16.0.0/12,192.168.0.0/16}")
    private String trustedProxiesConfig;

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

        if (!isAllowed(clientIp)) {
            log.warn("Rate limit exceeded for client IP: {}", clientIp);
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            response.getHeaders().add("Retry-After", String.valueOf(windowSeconds));

            String jsonError = String.format(
                    "{\"error\": \"Too Many Requests\", \"message\": \"Rate limit of %d requests per %d seconds exceeded.\", \"status\": 429}",
                    maxRequestsPerMinute, windowSeconds
            );
            byte[] bytes = jsonError.getBytes(StandardCharsets.UTF_8);

            return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
        }

        return chain.filter(exchange);
    }

    private boolean isAllowed(String clientIp) {
        long now = System.currentTimeMillis();
        long windowMillis = windowSeconds * 1000L;

        // Cleanup stale entries
        requestCounts.entrySet().removeIf(entry -> (now - entry.getValue().startTime) > windowMillis);

        RequestCounter counter = requestCounts.compute(clientIp, (ip, existing) -> {
            if (existing == null || (now - existing.startTime) > windowMillis) {
                return new RequestCounter(now);
            }
            existing.count.incrementAndGet();
            return existing;
        });

        return counter.count.get() <= maxRequestsPerMinute;
    }

    private String getClientIp(ServerWebExchange exchange) {
        InetSocketAddress remoteSocketAddress = exchange.getRequest().getRemoteAddress();
        if (remoteSocketAddress == null || remoteSocketAddress.getAddress() == null) {
            return "unknown";
        }

        InetAddress remoteAddress = remoteSocketAddress.getAddress();
        String directIp = remoteAddress.getHostAddress();

        // Validate whether incoming hop is from a trusted proxy
        if (isTrustedProxy(remoteAddress)) {
            String xffHeader = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
            if (xffHeader != null && !xffHeader.trim().isEmpty()) {
                String[] ips = xffHeader.split(",");
                String clientIp = ips[0].trim();
                if (isValidIp(clientIp)) {
                    return clientIp;
                }
            }
        }

        return directIp;
    }

    private boolean isTrustedProxy(InetAddress remoteAddress) {
        if (remoteAddress == null) {
            return false;
        }

        if (remoteAddress.isLoopbackAddress() || remoteAddress.isSiteLocalAddress()) {
            return true;
        }

        String host = remoteAddress.getHostAddress();
        if (trustedProxiesConfig != null) {
            List<String> trustedList = Arrays.stream(trustedProxiesConfig.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());

            for (String trusted : trustedList) {
                if (trusted.equalsIgnoreCase(host)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isValidIp(String ip) {
        if (ip == null || ip.isBlank() || ip.length() > 45) {
            return false;
        }
        return ip.matches("^[0-9a-fA-F:.]+$");
    }

    @Override
    public int getOrder() {
        return -3;
    }
}
