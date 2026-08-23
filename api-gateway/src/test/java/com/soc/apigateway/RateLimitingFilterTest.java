package com.soc.apigateway;

import com.soc.apigateway.filter.RateLimitingFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.InetSocketAddress;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RateLimitingFilterTest {

    @Mock
    private GatewayFilterChain chain;

    @InjectMocks
    private RateLimitingFilter filter;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(filter, "maxRequestsPerMinute", 60);
        ReflectionTestUtils.setField(filter, "windowSeconds", 60);
        ReflectionTestUtils.setField(filter, "trustedProxiesConfig", "127.0.0.1,::1,10.0.0.0/8,172.16.0.0/12,192.168.0.0/16");
    }

    @Test
    void testRateLimit_UnderThreshold_Allowed() {
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/products")
                .remoteAddress(new InetSocketAddress("203.0.113.195", 54321))
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        verify(chain, times(1)).filter(any(ServerWebExchange.class));
    }

    @Test
    void testRateLimit_ExceedThreshold_Returns429TooManyRequests() {
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        InetSocketAddress clientAddress = new InetSocketAddress("198.51.100.42", 54321);

        // Send 60 requests -> all should pass
        for (int i = 1; i <= 60; i++) {
            MockServerHttpRequest req = MockServerHttpRequest.get("/api/products")
                    .remoteAddress(clientAddress)
                    .build();
            ServerWebExchange ex = MockServerWebExchange.from(req);
            StepVerifier.create(filter.filter(ex, chain)).verifyComplete();
        }

        // 61st request -> should return 429
        MockServerHttpRequest req61 = MockServerHttpRequest.get("/api/products")
                .remoteAddress(clientAddress)
                .build();
        ServerWebExchange ex61 = MockServerWebExchange.from(req61);

        StepVerifier.create(filter.filter(ex61, chain))
                .verifyComplete();

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, ex61.getResponse().getStatusCode());
    }

    @Test
    void testUntrustedDirectClient_CannotSpoofXForwardedFor() {
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        // Direct connection from public IP (not a trusted proxy)
        InetSocketAddress directPublicIp = new InetSocketAddress("203.0.113.50", 12345);

        // Send 60 requests with varying spoofed XFF headers
        for (int i = 1; i <= 60; i++) {
            MockServerHttpRequest req = MockServerHttpRequest.get("/api/products")
                    .remoteAddress(directPublicIp)
                    .header("X-Forwarded-For", "1.2.3." + i)
                    .build();
            ServerWebExchange ex = MockServerWebExchange.from(req);
            StepVerifier.create(filter.filter(ex, chain)).verifyComplete();
        }

        // 61st request from same direct IP with yet another spoofed XFF header
        MockServerHttpRequest req61 = MockServerHttpRequest.get("/api/products")
                .remoteAddress(directPublicIp)
                .header("X-Forwarded-For", "1.2.3.99")
                .build();
        ServerWebExchange ex61 = MockServerWebExchange.from(req61);

        StepVerifier.create(filter.filter(ex61, chain)).verifyComplete();

        // Must still be blocked with 429 because direct IP was tracked
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, ex61.getResponse().getStatusCode());
    }
}
