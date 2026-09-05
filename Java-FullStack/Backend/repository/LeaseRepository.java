package com.jetlease.repository;

import com.jetlease.entity.Lease;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface LeaseRepository extends JpaRepository<Lease, String> {
    Optional<Lease> findByBookingId(String bookingId);
    List<Lease> findByUserEmailOrderByCreatedAtDesc(String userEmail);
    List<Lease> findAllByOrderByCreatedAtDesc();
}
