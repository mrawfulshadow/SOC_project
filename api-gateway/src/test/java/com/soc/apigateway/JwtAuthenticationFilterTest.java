package com.soc.apigateway;

import com.soc.apigateway.filter.JwtAuthenticationFilter;
import com.soc.apigateway.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private GatewayFilterChain chain;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(filter, "apiKeyProduct", "PRODUCT-SERVICE-SECRET-KEY");
        ReflectionTestUtils.setField(filter, "apiKeyOrder", "order-secret-key-123");
        ReflectionTestUtils.setField(filter, "apiKeyPayment", "payment-secret-key-123");
        ReflectionTestUtils.setField(filter, "apiKeyNotification", "notification-secret-key-123");
    }

    @Test
    void testOpenEndpoint_BypassesAuth() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/auth/login").build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        verify(chain, times(1)).filter(any(ServerWebExchange.class));
        verify(jwtUtil, never()).validateToken(anyString());
    }

    @Test
    void testProtectedEndpoint_MissingToken_Returns401() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/products").build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(chain, never()).filter(any(ServerWebExchange.class));
    }

    @Test
    void testProtectedEndpoint_InvalidOrExpiredToken_Returns401() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/products")
                .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-tampered-token")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        when(jwtUtil.validateToken("invalid-tampered-token")).thenReturn(false);

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(chain, never()).filter(any(ServerWebExchange.class));
    }

    @Test
    void testProtectedEndpoint_ValidToken_SuccessAndInjectsHeaders() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/products")
                .header(HttpHeaders.AUTHORIZATION, "Bearer valid-token")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        Claims mockClaims = mock(Claims.class);
        when(mockClaims.getSubject()).thenReturn("john_doe");
        when(mockClaims.get("role", String.class)).thenReturn("ROLE_USER");

        when(jwtUtil.validateToken("valid-token")).thenReturn(true);
        when(jwtUtil.getClaimsFromToken("valid-token")).thenReturn(mockClaims);
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        verify(chain, times(1)).filter(any(ServerWebExchange.class));
    }

    @Test
    void testPathTraversalBypassAttempt_Returns401() {
        // Query param bypass attempt: /api/orders?x=/swagger-ui
        MockServerHttpRequest request1 = MockServerHttpRequest.get("/api/orders?x=/swagger-ui").build();
        ServerWebExchange exchange1 = MockServerWebExchange.from(request1);

        StepVerifier.create(filter.filter(exchange1, chain))
                .verifyComplete();
        assertEquals(HttpStatus.UNAUTHORIZED, exchange1.getResponse().getStatusCode());

        // Path traversal sequence: /api/orders/../api/auth/login
        MockServerHttpRequest request2 = MockServerHttpRequest.get("/api/orders/../api/auth/login").build();
        ServerWebExchange exchange2 = MockServerWebExchange.from(request2);

        StepVerifier.create(filter.filter(exchange2, chain))
                .verifyComplete();
        assertEquals(HttpStatus.UNAUTHORIZED, exchange2.getResponse().getStatusCode());
    }
}
