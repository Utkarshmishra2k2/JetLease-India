package com.jetlease.model.entity;

public class Payment {
    private String id;
    private String bookingId;
    private String userEmail;
    private long amount;
    private String transactionId;
    private String status;
    private String submittedAt;
    private long cancellationFee;
    private long refundAmount;

    public Payment() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public long getAmount() { return amount; }
    public void setAmount(long amount) { this.amount = amount; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(String submittedAt) { this.submittedAt = submittedAt; }

    public long getCancellationFee() { return cancellationFee; }
    public void setCancellationFee(long cancellationFee) { this.cancellationFee = cancellationFee; }

    public long getRefundAmount() { return refundAmount; }
    public void setRefundAmount(long refundAmount) { this.refundAmount = refundAmount; }
}
