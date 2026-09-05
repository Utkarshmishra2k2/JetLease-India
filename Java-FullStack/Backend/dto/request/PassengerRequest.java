package com.jetlease.dto.request;

import lombok.Data;

@Data
public class PassengerRequest {
    private String name;
    private String dob;
    private String gender;
    private String aadhaar;
    private String verificationStatus; // set by client after calling verify-aadhaar, or "Not Applicable"/"Not Required"
    private boolean noAadhaar;
    private String altDocumentId;
}
