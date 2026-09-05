package com.jetlease.dto.request;

import lombok.Data;

@Data
public class ContactRequest {
    private String name;
    private String phone;
    private String email;
    private String message;
}
