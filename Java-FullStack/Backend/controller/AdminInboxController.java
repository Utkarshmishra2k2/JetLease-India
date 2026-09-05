package com.jetlease.controller;

import com.jetlease.entity.ContactMessage;
import com.jetlease.entity.Report;
import com.jetlease.security.AuthContext;
import com.jetlease.security.CurrentUser;
import com.jetlease.service.AdminService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/inbox")
public class AdminInboxController {

    private final AdminService adminService;
    private final AuthContext authContext;

    public AdminInboxController(AdminService adminService, AuthContext authContext) {
        this.adminService = adminService;
        this.authContext = authContext;
    }

    @GetMapping("/messages")
    public List<ContactMessage> messages(HttpServletRequest request) {
        authContext.requireAdmin(request);
        return adminService.allContactMessages();
    }

    @PostMapping("/messages/{id}/mark-read")
    public ContactMessage markRead(@PathVariable String id, HttpServletRequest request) {
        CurrentUser admin = authContext.requireAdmin(request);
        return adminService.markMessageRead(admin.email, id);
    }

    @GetMapping("/reports")
    public List<Report> reports(HttpServletRequest request) {
        authContext.requireAdmin(request);
        return adminService.allReports();
    }

    @PostMapping("/reports/{id}/resolve")
    public Report resolve(@PathVariable String id, HttpServletRequest request) {
        CurrentUser admin = authContext.requireAdmin(request);
        return adminService.resolveReport(admin.email, id);
    }
}
