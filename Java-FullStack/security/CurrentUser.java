package com.jetlease.security;

public class CurrentUser {
    public final String email;
    public final String role;

    public CurrentUser(String email, String role) {
        this.email = email;
        this.role = role;
    }
}
