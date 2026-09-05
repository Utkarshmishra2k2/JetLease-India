package com.jetlease.dto.request;

import lombok.Data;

@Data
public class ChangePhoneRequest {
    private String newPhone;
    private String otp;
}
