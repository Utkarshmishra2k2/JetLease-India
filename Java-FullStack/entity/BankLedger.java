package com.jetlease.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "bank_ledger")
@Getter
@Setter
public class BankLedger {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_id")
    private String transactionId;

    @Column(name = "booking_id")
    private String bookingId;

    private long amount;
    private String status;

    @Column(name = "cleared_at")
    private String clearedAt;
}
