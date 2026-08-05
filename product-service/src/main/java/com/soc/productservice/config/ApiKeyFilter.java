package com.soc.productservice.config;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class ApiKeyFilter implements Filter {
    private static final String API_KEY_HEADER = "X-API-KEY";
    private static final String VALID_API_KEY = "PRODUCT-SERVICE-SECRET-KEY";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String path = req.getRequestURI();
        // Swagger UI සහ API docs වලට API Key එක නැතිව යාමට ඉඩ දීම
        if (path.contains("swagger") || path.contains("api-docs")) {
            chain.doFilter(request, response);
            return;
        }

        String apiKey = req.getHeader(API_KEY_HEADER);
        if (VALID_API_KEY.equals(apiKey)) {
            chain.doFilter(request, response);
        } else {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.getWriter().write("Unauthorized: Invalid or Missing API Key");
        }
    }
}