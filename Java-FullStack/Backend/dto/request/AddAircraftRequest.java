package com.jetlease.dto.request;

import lombok.Data;

@Data
public class AddAircraftRequest {
    private String reg;
    private String model;
    private String manufacturer;
    private String category;
    private int capacity;
    private int speed;
    private int rangeKm;
    private long hourlyRate;
    private String typeRating;
}
