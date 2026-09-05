package com.jetlease.security;

public class SessionInfo {
    public final String token;
    public final String email;
    public final String role; // customer | admin
    public final long createdAtMillis;

    public SessionInfo(String token, String email, String role) {
        this.token = token;
        this.email = email;
        this.role = role;
        this.createdAtMillis = System.currentTimeMillis();
    }
}
