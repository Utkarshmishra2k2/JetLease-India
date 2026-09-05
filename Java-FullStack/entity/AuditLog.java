package com.jetlease.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "audit_log")
@Getter
@Setter
public class AuditLog {
    @Id
    private String id;

    private String actor;
    private String category; // Login | Booking | Payment | Lease | Admin
    private String action;

    @Column(length = 2000)
    private String details;

    private String timestamp;
}
