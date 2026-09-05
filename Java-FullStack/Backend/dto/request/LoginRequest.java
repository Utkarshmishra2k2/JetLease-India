package com.jetlease.dto.request;

import lombok.Data;

@Data
public class LoginRequest {
    /** "email" or "phone" */
    private String identifierType;
    private String identifier;
    private String password;
}
