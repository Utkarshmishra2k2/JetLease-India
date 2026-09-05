package com.jetlease.security;

import com.jetlease.exception.ForbiddenException;
import com.jetlease.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

/**
 * Resolves the current authenticated user from the Authorization header
 * for each request and enforces role requirements.
 */
@Component
public class AuthContext {

    private final SessionService sessionService;

    public AuthContext(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) return null;
        return header.substring(7).trim();
    }

    public CurrentUser requireCustomer(HttpServletRequest request) {
        CurrentUser user = requireAuth(request);
        if (!"customer".equals(user.role)) {
            throw new ForbiddenException("Customer account required.");
        }
        return user;
    }

    public CurrentUser requireAdmin(HttpServletRequest request) {
        CurrentUser user = requireAuth(request);
        if (!"admin".equals(user.role)) {
            throw new ForbiddenException("Admin account required.");
        }
        return user;
    }

    public CurrentUser requireAuth(HttpServletRequest request) {
        String token = extractToken(request);
        SessionInfo info = sessionService.getSession(token);
        if (info == null) {
            throw new UnauthorizedException("Session expired or invalid. Please log in again.");
        }
        return new CurrentUser(info.email, info.role);
    }
}
