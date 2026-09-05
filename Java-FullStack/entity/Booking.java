package com.jetlease.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "bookings")
@Getter
@Setter
public class Booking {
    @Id
    private String id;

    @Column(name = "user_email")
    private String userEmail;

    private String type;       // Domestic Charter | Helicopter Charter

    @Column(name = "trip_type")
    private String tripType;   // One Way | Round Trip

    private String origin;
    private String destination;
    private String date;
    private String time;

    @Column(name = "return_date")
    private String returnDate;

    @Column(name = "return_time")
    private String returnTime;

    private int pax;

    @Column(name = "aircraft_id")
    private String aircraftId;

    @Column(name = "aircraft_model")
    private String aircraftModel;

    @Column(name = "self_fly")
    private boolean selfFly;

    @Column(name = "license_number")
    private String licenseNumber;

    @Column(name = "license_class")
    private String licenseClass;

    @Column(name = "flying_hours")
    private int flyingHours;

    @Column(name = "certificate_file_name")
    private String certificateFileName;

    @Column(name = "dgca_declaration")
    private boolean dgcaDeclaration;

    @Column(name = "license_verified")
    private boolean licenseVerified;

    private double hours;

    @Column(name = "aircraft_cost")
    private long aircraftCost;

    @Column(name = "pilot_cost")
    private long pilotCost;

    @Column(name = "crew_cost")
    private long crewCost;

    @Column(name = "airport_charges")
    private long airportCharges;

    @Column(name = "fuel_surcharge")
    private long fuelSurcharge;

    private long gst;
    private long total;

    private String status;

    @Column(name = "assigned_pilot_id")
    private String assignedPilotId;

    @Column(name = "assigned_crew_ids")
    private String assignedCrewIds;

    @Column(name = "created_at")
    private String createdAt;
}
