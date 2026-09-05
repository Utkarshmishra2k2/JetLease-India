package com.jetlease.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class AssignCrewRequest {
    private String pilotId;
    private List<String> crewIds;
}
