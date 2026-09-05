package com.jetlease.repository;

import com.jetlease.entity.AadhaarRegistry;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AadhaarRegistryRepository extends JpaRepository<AadhaarRegistry, Long> {
    Optional<AadhaarRegistry> findByAadhaarNumber(String aadhaarNumber);
}
