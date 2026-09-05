package com.jetlease.model.service;

import java.sql.SQLException;

import com.jetlease.model.dao.BookingDao;
import com.jetlease.model.dao.PaymentDao;
import com.jetlease.model.entity.Booking;
import com.jetlease.model.entity.Payment;

public class PaymentService {

    public static Booking getPayableBooking(String userEmail, String bookingId) throws SQLException {
        Booking b = BookingDao.findById(bookingId);
        if (b == null || !b.getUserEmail().equals(userEmail)) return null;
        return b;
    }

    public static void submitPayment(String userEmail, String bookingId, long totalAmount, String txnId) throws SQLException {
        MockApi.recordLedgerEntry(txnId, bookingId, totalAmount);

        String paymentId = IdGen.uid("PAY");
        Payment p = new Payment();
        p.setId(paymentId);
        p.setBookingId(bookingId);
        p.setUserEmail(userEmail);
        p.setAmount(totalAmount);
        p.setTransactionId(txnId);
        p.setStatus("PENDING_VERIFICATION");
        p.setSubmittedAt(IdGen.nowIso());
        PaymentDao.save(p);

        BookingDao.updateStatus(bookingId, "Pending Verification");

        BookingRules.addAudit(userEmail, "Payment", "Payment Submitted", bookingId + " - " + txnId);
        BookingRules.addNotification("admin", "Payment Submitted", "Payment submitted for " + bookingId + " by " + userEmail, "info");
        BookingRules.addNotification(userEmail, "Payment Submitted",
                "We received your transaction ID for " + bookingId + ". Verification is in progress.", "info");
    }
}

