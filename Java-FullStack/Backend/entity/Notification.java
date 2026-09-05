package com.jetlease.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "notifications")
@Getter
@Setter
public class Notification {
    @Id
    private String id;

    @Column(name = "user_email")
    private String userEmail;

    private String title;

    @Column(length = 2000)
    private String message;

    private String type; // info | success | warning

    @Column(name = "is_read")
    private boolean read;

    @Column(name = "created_at")
    private String createdAt;
}
