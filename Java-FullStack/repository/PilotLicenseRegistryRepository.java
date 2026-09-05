package com.jetlease.repository;

import com.jetlease.entity.PilotLicenseRegistry;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PilotLicenseRegistryRepository extends JpaRepository<PilotLicenseRegistry, Long> {
    Optional<PilotLicenseRegistry> findByLicenseNumberIgnoreCase(String licenseNumber);
}
