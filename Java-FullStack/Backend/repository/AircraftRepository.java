package com.jetlease.repository;

import com.jetlease.entity.Aircraft;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AircraftRepository extends JpaRepository<Aircraft, String> {
    List<Aircraft> findByStatusAndCapacityGreaterThanEqual(String status, int pax);
    List<Aircraft> findByStatusAndCategoryAndCapacityGreaterThanEqual(String status, String category, int pax);
}
