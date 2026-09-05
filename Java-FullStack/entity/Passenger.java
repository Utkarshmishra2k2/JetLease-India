package com.jetlease.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "passengers")
@Getter
@Setter
public class Passenger {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "booking_id")
    private String bookingId;

    private String name;
    private String dob;
    private String gender;
    private String aadhaar;

    @Column(name = "verification_status")
    private String verificationStatus;

    @Column(name = "no_aadhaar")
    private boolean noAadhaar;

    @Column(name = "alt_document_id")
    private String altDocumentId;
}
