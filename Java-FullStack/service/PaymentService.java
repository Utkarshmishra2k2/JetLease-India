package com.jetlease.service;

import com.jetlease.entity.Booking;
import com.jetlease.entity.Payment;
import com.jetlease.exception.BadRequestException;
import com.jetlease.exception.NotFoundException;
import com.jetlease.repository.BookingRepository;
import com.jetlease.repository.PaymentRepository;
import com.jetlease.util.IdGen;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PaymentService {

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final MockApiService mockApiService;
    private final BookingRulesService bookingRulesService;

    public PaymentService(BookingRepository bookingRepository, PaymentRepository paymentRepository,
                           MockApiService mockApiService, BookingRulesService bookingRulesService) {
        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
        this.mockApiService = mockApiService;
        this.bookingRulesService = bookingRulesService;
    }

    public Booking getPayableBooking(String userEmail, String bookingId) {
        Booking b = bookingRepository.findById(bookingId).orElseThrow(() -> new NotFoundException("Booking not found."));
        if (!b.getUserEmail().equals(userEmail)) throw new BadRequestException("This booking does not belong to you.");
        if (!bookingRulesService.isPayable(b.getStatus())) {
            throw new BadRequestException("Booking status \"" + b.getStatus() + "\" is not payable.");
        }
        return b;
    }

    @Transactional
    public Payment submitPayment(String userEmail, String bookingId, String txnId) {
        Booking b = getPayableBooking(userEmail, bookingId);
        if (txnId == null || txnId.isBlank()) throw new BadRequestException("Transaction ID is required.");

        mockApiService.recordLedgerEntry(txnId, bookingId, b.getTotal());

        Payment p = new Payment();
        p.setId(IdGen.uid("PAY"));
        p.setBookingId(bookingId);
        p.setUserEmail(userEmail);
        p.setAmount(b.getTotal());
        p.setTransactionId(txnId);
        p.setStatus("PENDING_VERIFICATION");
        p.setSubmittedAt(IdGen.nowIso());
        paymentRepository.save(p);

        b.setStatus("Pending Verification");
        bookingRepository.save(b);

        bookingRulesService.addAudit(userEmail, "Payment", "Payment Submitted", bookingId + " - " + txnId);
        bookingRulesService.addNotification("admin", "Payment Submitted", "Payment submitted for " + bookingId + " by " + userEmail, "info");
        bookingRulesService.addNotification(userEmail, "Payment Submitted",
                "We received your transaction ID for " + bookingId + ". Verification is in progress.", "info");
        return p;
    }

    public List<Payment> findByUser(String userEmail) {
        return paymentRepository.findByUserEmailOrderBySubmittedAtDesc(userEmail);
    }

    public List<Payment> findAll() {
        return paymentRepository.findAllByOrderBySubmittedAtDesc();
    }

    public Payment findById(String id) {
        return paymentRepository.findById(id).orElseThrow(() -> new NotFoundException("Payment not found."));
    }
}
