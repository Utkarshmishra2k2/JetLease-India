package com.jetlease.exception;

import java.time.Instant;
import java.util.Map;

public class ErrorResponse {
    public int status;
    public String message;
    public String timestamp;
    public Map<String, String> fieldErrors;

    public ErrorResponse(int status, String message) {
        this.status = status;
        this.message = message;
        this.timestamp = Instant.now().toString();
    }
}
