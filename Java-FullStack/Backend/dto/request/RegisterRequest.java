package com.jetlease.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "Full name is required.")
    private String fullName;

    @NotBlank(message = "Email is required.")
    private String email;

    @NotBlank(message = "Phone is required.")
    private String phone;

    @NotBlank(message = "Date of birth is required.")
    private String dob;

    @NotBlank(message = "Emergency contact is required.")
    private String emergencyContact;

    @NotBlank(message = "Password is required.")
    private String password;

    @NotBlank(message = "Please confirm your password.")
    private String confirmPassword;
}
