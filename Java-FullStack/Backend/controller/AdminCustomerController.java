package com.jetlease.controller;

import com.jetlease.entity.User;
import com.jetlease.security.AuthContext;
import com.jetlease.security.CurrentUser;
import com.jetlease.service.AdminService;
import com.jetlease.service.BookingService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/customers")
public class AdminCustomerController {

    private final AdminService adminService;
    private final BookingService bookingService;
    private final AuthContext authContext;

    public AdminCustomerController(AdminService adminService, BookingService bookingService, AuthContext authContext) {
        this.adminService = adminService;
        this.bookingService = bookingService;
        this.authContext = authContext;
    }

    @GetMapping
    public List<User> all(HttpServletRequest request) {
        authContext.requireAdmin(request);
        return adminService.allCustomers();
    }

    @GetMapping("/{email}/bookings")
    public Object bookings(@PathVariable String email, HttpServletRequest request) {
        authContext.requireAdmin(request);
        return bookingService.findByUser(email);
    }

    @PostMapping("/{email}/toggle-status")
    public Map<String, Object> toggle(@PathVariable String email, HttpServletRequest request) {
        CurrentUser admin = authContext.requireAdmin(request);
        User u = adminService.toggleCustomerStatus(admin.email, email);
        return Map.of("email", u.getEmail(), "status", u.getStatus());
    }
}
