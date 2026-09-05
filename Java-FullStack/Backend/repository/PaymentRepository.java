package com.jetlease.repository;

import com.jetlease.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, String> {
    Optional<Payment> findByBookingId(String bookingId);
    List<Payment> findByUserEmailOrderBySubmittedAtDesc(String userEmail);
    List<Payment> findAllByOrderBySubmittedAtDesc();
}
