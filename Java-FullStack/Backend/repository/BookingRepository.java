package com.jetlease.repository;

import com.jetlease.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, String> {
    List<Booking> findByUserEmailOrderByCreatedAtDesc(String userEmail);
    List<Booking> findAllByOrderByCreatedAtDesc();
    List<Booking> findByOriginOrDestination(String origin, String destination);
    long countByAircraftModel(String aircraftModel);
}
