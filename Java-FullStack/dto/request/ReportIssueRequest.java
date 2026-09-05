package com.jetlease.dto.request;

import lombok.Data;

@Data
public class ReportIssueRequest {
    private String bookingId;
    private String subject;
    private String details;
}
