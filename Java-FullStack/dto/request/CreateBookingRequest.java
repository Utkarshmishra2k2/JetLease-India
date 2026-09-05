package com.jetlease.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class CreateBookingRequest {
    private String type;        // Domestic Charter | Helicopter Charter
    private String tripType;    // One Way | Round Trip
    private String origin;
    private String destination;
    private String date;
    private String time;
    private String returnDate;
    private String returnTime;
    private int pax;
    private String aircraftId;
    private boolean selfFly;
    private SelfFlyRequest selfFlyDetails; // required if selfFly = true
    private List<PassengerRequest> passengers;
}
