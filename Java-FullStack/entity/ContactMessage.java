package com.jetlease.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "contact_messages")
@Getter
@Setter
public class ContactMessage {
    @Id
    private String id;

    private String name;
    private String phone;
    private String email;

    @Column(length = 2000)
    private String message;

    private String status; // Unread | Read

    @Column(name = "created_at")
    private String createdAt;
}
