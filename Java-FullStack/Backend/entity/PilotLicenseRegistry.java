package com.jetlease.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "pilot_license_registry")
@Getter
@Setter
public class PilotLicenseRegistry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "license_number", unique = true)
    private String licenseNumber;

    @Column(name = "holder_name")
    private String holderName;

    @Column(name = "license_class")
    private String licenseClass;

    @Column(name = "hours_on_record")
    private int hoursOnRecord;

    private String status; // Active | Suspended | Expired
}
