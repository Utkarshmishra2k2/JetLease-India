package com.jetlease.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "aadhaar_registry")
@Getter
@Setter
public class AadhaarRegistry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "aadhaar_number", unique = true)
    private String aadhaarNumber;

    @Column(name = "holder_name")
    private String holderName;

    private String dob;
    private String gender;
    private String status; // Active | Inactive
}
