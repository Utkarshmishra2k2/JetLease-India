package com.jetlease.dto.request;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String fullName;
    private String dob;
    private String emergencyContact;
}
