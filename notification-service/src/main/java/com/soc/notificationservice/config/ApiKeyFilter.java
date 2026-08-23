package com.soc.notificationservice.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
public class ApiKeyFilter implements Filter {

    private static final String API_KEY_HEADER = "X-API-KEY";

    @Value("${api.key:${NOTIFICATION_API_KEY:notification-secret-key-123}}")
    private String validApiKey;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String path = req.getRequestURI();

        if (path != null && (path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs") || path.startsWith("/api-docs"))) {
            chain.doFilter(request, response);
            return;
        }

        String apiKey = req.getHeader(API_KEY_HEADER);

        if (apiKey == null || validApiKey == null || !MessageDigest.isEqual(
                apiKey.getBytes(StandardCharsets.UTF_8),
                validApiKey.getBytes(StandardCharsets.UTF_8))) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.setContentType("application/json");
            res.getWriter().write("{\"error\": \"Unauthorized: Invalid or missing API Key for Notification Service\"}");
            return;
        }

        chain.doFilter(request, response);
    }
}
