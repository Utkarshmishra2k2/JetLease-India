package com.jetlease.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "reports")
@Getter
@Setter
public class Report {
    @Id
    private String id;

    @Column(name = "booking_id")
    private String bookingId;

    @Column(name = "user_email")
    private String userEmail;

    private String subject;

    @Column(length = 2000)
    private String details;

    private String status; // Open | Resolved

    @Column(name = "created_at")
    private String createdAt;
}
