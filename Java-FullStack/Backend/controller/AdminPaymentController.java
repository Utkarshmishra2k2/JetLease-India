package com.jetlease.controller;

import com.jetlease.entity.Payment;
import com.jetlease.security.AuthContext;
import com.jetlease.security.CurrentUser;
import com.jetlease.service.AdminService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/payments")
public class AdminPaymentController {

    private final AdminService adminService;
    private final AuthContext authContext;

    public AdminPaymentController(AdminService adminService, AuthContext authContext) {
        this.adminService = adminService;
        this.authContext = authContext;
    }

    @GetMapping
    public List<Payment> all(HttpServletRequest request) {
        authContext.requireAdmin(request);
        return adminService.allPayments();
    }

    @GetMapping("/{id}/ledger-check")
    public Map<String, Object> ledgerCheck(@PathVariable String id, HttpServletRequest request) {
        authContext.requireAdmin(request);
        return adminService.checkPaymentLedger(id);
    }

    @PostMapping("/{id}/verify")
    public Payment verify(@PathVariable String id, HttpServletRequest request) {
        CurrentUser admin = authContext.requireAdmin(request);
        return adminService.verifyPayment(admin.email, id);
    }

    @PostMapping("/{id}/reject")
    public Payment reject(@PathVariable String id, HttpServletRequest request) {
        CurrentUser admin = authContext.requireAdmin(request);
        return adminService.rejectPayment(admin.email, id);
    }
}
