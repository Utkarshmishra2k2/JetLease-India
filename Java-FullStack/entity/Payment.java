package com.jetlease.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "payments")
@Getter
@Setter
public class Payment {
    @Id
    private String id;

    @Column(name = "booking_id")
    private String bookingId;

    @Column(name = "user_email")
    private String userEmail;

    private long amount;

    @Column(name = "transaction_id")
    private String transactionId;

    private String status; // PENDING_VERIFICATION | VERIFIED | REJECTED | RETURNED

    @Column(name = "submitted_at")
    private String submittedAt;

    @Column(name = "cancellation_fee")
    private long cancellationFee;

    @Column(name = "refund_amount")
    private long refundAmount;
}
