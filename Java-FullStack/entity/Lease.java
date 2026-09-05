package com.jetlease.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "leases")
@Getter
@Setter
public class Lease {
    @Id
    private String id;

    @Column(name = "booking_id")
    private String bookingId;

    @Column(name = "user_email")
    private String userEmail;

    private String status; // Sent | Signed | Approved | Rejected

    @Column(name = "signed_by")
    private String signedBy;

    @Column(name = "signed_date")
    private String signedDate;

    @Column(name = "approval_date")
    private String approvalDate;

    @Column(name = "created_at")
    private String createdAt;
}
