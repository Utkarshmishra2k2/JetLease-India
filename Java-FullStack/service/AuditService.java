package com.jetlease.service;

import com.jetlease.entity.AuditLog;
import com.jetlease.repository.AuditLogRepository;
import com.jetlease.util.IdGen;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void addAudit(String actor, String category, String action, String details) {
        AuditLog log = new AuditLog();
        log.setId(IdGen.uid("AUD"));
        log.setActor(actor);
        log.setCategory(category);
        log.setAction(action);
        log.setDetails(details);
        log.setTimestamp(IdGen.nowIso());
        auditLogRepository.save(log);
    }

    public List<AuditLog> findAll(String category) {
        if (category == null || category.isBlank() || "All".equalsIgnoreCase(category)) {
            return auditLogRepository.findAllByOrderByTimestampDesc();
        }
        return auditLogRepository.findByCategoryOrderByTimestampDesc(category);
    }
}
