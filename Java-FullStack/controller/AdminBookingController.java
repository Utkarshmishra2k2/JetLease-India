package com.jetlease.controller;

import com.jetlease.dto.request.AssignCrewRequest;
import com.jetlease.entity.Booking;
import com.jetlease.security.AuthContext;
import com.jetlease.security.CurrentUser;
import com.jetlease.service.AdminService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/bookings")
public class AdminBookingController {

    private final AdminService adminService;
    private final AuthContext authContext;

    public AdminBookingController(AdminService adminService, AuthContext authContext) {
        this.adminService = adminService;
        this.authContext = authContext;
    }

    @GetMapping
    public List<Booking> all(HttpServletRequest request) {
        authContext.requireAdmin(request);
        return adminService.allBookings();
    }

    @GetMapping("/{id}")
    public Booking get(@PathVariable String id, HttpServletRequest request) {
        authContext.requireAdmin(request);
        return adminService.getBooking(id);
    }

    @PostMapping("/{id}/assign-crew")
    public Booking assignCrew(@PathVariable String id, @RequestBody AssignCrewRequest req, HttpServletRequest request) {
        CurrentUser admin = authContext.requireAdmin(request);
        return adminService.assignCrew(admin.email, id, req);
    }

    @PostMapping("/{id}/approve")
    public Booking approve(@PathVariable String id, HttpServletRequest request) {
        CurrentUser admin = authContext.requireAdmin(request);
        return adminService.advanceBooking(admin.email, id, "Approved");
    }

    @PostMapping("/{id}/dispatch")
    public Booking dispatch(@PathVariable String id, HttpServletRequest request) {
        CurrentUser admin = authContext.requireAdmin(request);
        return adminService.advanceBooking(admin.email, id, "Dispatched");
    }

    @PostMapping("/{id}/complete")
    public Booking complete(@PathVariable String id, HttpServletRequest request) {
        CurrentUser admin = authContext.requireAdmin(request);
        return adminService.completeBooking(admin.email, id);
    }

    @PostMapping("/{id}/reject")
    public Booking reject(@PathVariable String id, HttpServletRequest request) {
        CurrentUser admin = authContext.requireAdmin(request);
        return adminService.rejectBooking(admin.email, id);
    }
}
