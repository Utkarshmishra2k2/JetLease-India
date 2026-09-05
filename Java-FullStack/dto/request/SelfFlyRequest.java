package com.jetlease.dto.request;

import lombok.Data;

@Data
public class SelfFlyRequest {
    private String licenseNumber;
    private String licenseClass;
    private int flyingHours;
    private boolean dgcaDeclaration;
    private boolean verified; // set by client after calling verify-license
    private String certificateFileName;
}
