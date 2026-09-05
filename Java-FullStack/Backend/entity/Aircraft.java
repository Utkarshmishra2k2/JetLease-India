package com.jetlease.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "aircraft")
@Getter
@Setter
public class Aircraft {
    @Id
    private String id;

    private String reg;
    private String model;
    private String manufacturer;
    private String category;
    private int capacity;
    private int speed;

    @Column(name = "range_km")
    private int rangeKm;

    @Column(name = "hourly_rate")
    private long hourlyRate;

    private String status; // Available | Booked | Maintenance | Grounded | Retired

    @Column(name = "type_rating")
    private String typeRating;
}
