package com.jetlease.controller;

import com.jetlease.entity.Lease;
import com.jetlease.security.AuthContext;
import com.jetlease.security.CurrentUser;
import com.jetlease.service.AdminService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/leases")
public class AdminLeaseController {

    private final AdminService adminService;
    private final AuthContext authContext;

    public AdminLeaseController(AdminService adminService, AuthContext authContext) {
        this.adminService = adminService;
        this.authContext = authContext;
    }

    @GetMapping
    public List<Lease> all(HttpServletRequest request) {
        authContext.requireAdmin(request);
        return adminService.allLeases();
    }

    @PostMapping("/{id}/approve")
    public Lease approve(@PathVariable String id, HttpServletRequest request) {
        CurrentUser admin = authContext.requireAdmin(request);
        return adminService.approveLease(admin.email, id);
    }

    @PostMapping("/{id}/reject")
    public Lease reject(@PathVariable String id, HttpServletRequest request) {
        CurrentUser admin = authContext.requireAdmin(request);
        return adminService.rejectLease(admin.email, id);
    }
}
