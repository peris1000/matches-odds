package com.zimono.sports_odds.config;

import com.zimono.sports_odds.service.RateLimitingService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {
    private final RateLimitingService service;
    private final RateLimitingProperties properties;

    public RateLimitingFilter(RateLimitingService service, RateLimitingProperties properties) {
        this.service = service;
        this.properties = properties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        if (!properties.isEnabled()) {
            chain.doFilter(request, response);
            return;
        }

        // Apply only to the specific endpoint(s)
        String requestURI = request.getRequestURI();
        if (!shouldRateLimit(requestURI)) {
            chain.doFilter(request, response);
            return;
        }

        // Build a unique key per user and endpoint
        String userId = extractUserId(request);
        String key = String.format("%s:%s:%s", userId, request.getMethod(), requestURI);

        boolean allowed = service.isAllowed(key);

        if (!allowed) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write("Rate limit exceeded. Please try again later.");
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean shouldRateLimit(String uri) {
        // Configure the protected endpoint(s)
        return uri.startsWith("/api/teams") || uri.equals("/api/matches");
    }

    private String extractUserId(HttpServletRequest request) {

        // try authentication from context
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && !auth.getName().equals("anonymousUser")) {
            return auth.getName();
        }

        // Fallback to IP address
        return request.getRemoteAddr();
    }
}
