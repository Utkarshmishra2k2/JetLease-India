package com.jetlease.dto.request;

import lombok.Data;

@Data
public class RecommendRequest {
    private int pax;
    private long budget;
    private int distanceKm;
    private String category; // nullable
}
