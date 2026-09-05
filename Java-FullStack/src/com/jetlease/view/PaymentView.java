package com.jetlease.view;

import java.util.List;
import static com.jetlease.view.ConsoleUtil.*;

import com.jetlease.model.entity.Payment;

public class PaymentView {

    public static void displayBankTransferDetails(long totalAmount) {
        printHeader("Bank Transfer Details");
        printLine("Account Name", "JetLease India Charters Pvt Ltd");
        printLine("Account Number", "0123456789012345");
        printLine("IFSC Code", "JLIN0001234");
        printLine("Amount to Pay", fmtInr(totalAmount));
        System.out.println("\nTransfer the amount above, then enter your transaction ID below.");
    }

    public static void renderPaymentsList(List<Payment> payments) {
        printHeader("My Payments");
        if (payments.isEmpty()) {
            System.out.println("No payments yet.");
        } else {
            for (Payment p : payments) {
                System.out.println();
                printLine("Payment ID", p.getId());
                printLine("Booking", p.getBookingId());
                printLine("Amount", fmtInr(p.getAmount()));
                printLine("Transaction ID", p.getTransactionId());
                printLine("Status", p.getStatus());
                printLine("Submitted", p.getSubmittedAt());
                if (p.getCancellationFee() > 0) printLine("Cancellation Fee", fmtInr(p.getCancellationFee()));
                if (p.getRefundAmount() > 0) printLine("Refund Amount", fmtInr(p.getRefundAmount()));
            }
        }
        pause();
    }
}
