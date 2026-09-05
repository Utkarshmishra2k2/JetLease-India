package com.jetlease.security;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lightweight in-memory bearer-token session store. Mirrors the original
 * console app's simple email-based "logged in as" session, adapted to a
 * stateless REST/SPA world without introducing a full OAuth2 stack.
 */
@Service
public class SessionService {

    private final Map<String, SessionInfo> sessions = new ConcurrentHashMap<>();

    public SessionInfo createSession(String email, String role) {
        String token = UUID.randomUUID().toString().replace("-", "");
        SessionInfo info = new SessionInfo(token, email, role);
        sessions.put(token, info);
        return info;
    }

    public SessionInfo getSession(String token) {
        if (token == null) return null;
        return sessions.get(token);
    }

    public void invalidate(String token) {
        if (token != null) sessions.remove(token);
    }
}
