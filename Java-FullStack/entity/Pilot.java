package com.jetlease.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "pilots")
@Getter
@Setter
public class Pilot {
    @Id
    private String id;

    private String name;

    @Column(name = "license_number")
    private String licenseNumber;

    @Column(name = "remaining_hours")
    private double remainingHours;

    private boolean available;
}
