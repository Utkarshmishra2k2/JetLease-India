package com.jetlease.controller;

import java.sql.SQLException;

import static com.jetlease.view.ConsoleUtil.*;

import com.jetlease.model.dao.PaymentDao;
import com.jetlease.model.entity.Booking;
import com.jetlease.model.service.BookingRules;
import com.jetlease.model.service.PaymentService;
import com.jetlease.view.PaymentView;

public class PaymentController {

    public static void payForBooking(String userEmail, String bookingId) throws SQLException {
        Booking b = PaymentService.getPayableBooking(userEmail, bookingId);
        if (b == null) {
            System.out.println("  ! Booking not found.");
            return;
        }

        if (!BookingRules.isPayable(b.getStatus())) {
            System.out.println("  ! This booking is currently \"" + b.getStatus() + "\" and is not awaiting payment.");
            return;
        }

        PaymentView.displayBankTransferDetails(b.getTotal());

        String txnId = readValidated("Transaction ID: ", v -> v.trim().length() < 5 ? "Transaction ID must be at least 5 characters." : "");

        PaymentService.submitPayment(userEmail, bookingId, b.getTotal(), txnId);

        System.out.println("\nPayment submitted. Our team will verify it shortly.");
        pause();
    }

    public static void listMyPayments(String userEmail) throws SQLException {
        PaymentView.renderPaymentsList(PaymentDao.findByUserEmail(userEmail));
    }
}
