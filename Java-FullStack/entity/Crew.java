package com.jetlease.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "crew")
@Getter
@Setter
public class Crew {
    @Id
    private String id;

    private String name;
    private String role;

    @Column(name = "remaining_hours")
    private double remainingHours;

    private boolean available;
}
