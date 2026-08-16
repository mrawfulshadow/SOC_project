package com.soc.notificationservice.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class ApiKeyFilter implements Filter {

    private static final String API_KEY_HEADER = "X-API-KEY";
    private static final String VALID_API_KEY = "notification-secret-key-123";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String path = req.getRequestURI();

        if (path.contains("/swagger-ui") || path.contains("/v3/api-docs") || path.contains("/api-docs")) {
            chain.doFilter(request, response);
            return;
        }

        String apiKey = req.getHeader(API_KEY_HEADER);

        if (apiKey == null || !apiKey.equals(VALID_API_KEY)) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.getWriter().write("Unauthorized: Invalid or missing API Key");
            return;
        }

        chain.doFilter(request, response);
    }
}
