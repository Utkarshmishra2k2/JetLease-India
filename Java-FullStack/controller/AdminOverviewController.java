package com.jetlease.controller;

import com.jetlease.entity.AuditLog;
import com.jetlease.security.AuthContext;
import com.jetlease.service.AdminService;
import com.jetlease.service.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminOverviewController {

    private final AdminService adminService;
    private final AuditService auditService;
    private final AuthContext authContext;

    public AdminOverviewController(AdminService adminService, AuditService auditService, AuthContext authContext) {
        this.adminService = adminService;
        this.auditService = auditService;
        this.authContext = authContext;
    }

    @GetMapping("/overview")
    public Map<String, Object> overview(HttpServletRequest request) {
        authContext.requireAdmin(request);
        return adminService.overview();
    }

    @GetMapping("/audit-log")
    public List<AuditLog> auditLog(@RequestParam(required = false) String category, HttpServletRequest request) {
        authContext.requireAdmin(request);
        return auditService.findAll(category);
    }

    @GetMapping("/routes")
    public List<Map<String, Object>> routes(HttpServletRequest request) {
        authContext.requireAdmin(request);
        return adminService.routesWithBookingCounts();
    }

    @GetMapping(value = "/exports/bookings.csv", produces = "text/csv")
    public ResponseEntity<String> exportBookings(HttpServletRequest request) {
        authContext.requireAdmin(request);
        return csvResponse("bookings.csv", adminService.exportBookingsCsv());
    }

    @GetMapping(value = "/exports/customers.csv", produces = "text/csv")
    public ResponseEntity<String> exportCustomers(HttpServletRequest request) {
        authContext.requireAdmin(request);
        return csvResponse("customers.csv", adminService.exportCustomersCsv());
    }

    @GetMapping(value = "/exports/payments.csv", produces = "text/csv")
    public ResponseEntity<String> exportPayments(HttpServletRequest request) {
        authContext.requireAdmin(request);
        return csvResponse("payments.csv", adminService.exportPaymentsCsv());
    }

    private ResponseEntity<String> csvResponse(String filename, String csv) {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv"))
                .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                .body(csv);
    }
}
