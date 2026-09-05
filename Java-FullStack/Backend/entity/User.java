package com.jetlease.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {
    @Id
    private String id;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String phone;

    private String dob;

    @Column(name = "emergency_contact")
    private String emergencyContact;

    @Column(nullable = false)
    private String password;

    private String country;

    @Column(nullable = false)
    private String role; // customer | admin

    @Column(nullable = false)
    private String status; // active | suspended

    private String membership; // none | silver | gold | platinum

    @Column(name = "loyalty_points")
    private int loyaltyPoints;

    @Column(name = "created_at")
    private String createdAt;
}
