package com.jetlease.repository;

import com.jetlease.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, String> {
    List<AuditLog> findAllByOrderByTimestampDesc();
    List<AuditLog> findByCategoryOrderByTimestampDesc(String category);
}
