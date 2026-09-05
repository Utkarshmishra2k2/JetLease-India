package com.jetlease.controller;

import com.jetlease.dto.request.SignLeaseRequest;
import com.jetlease.entity.Booking;
import com.jetlease.entity.Lease;
import com.jetlease.security.AuthContext;
import com.jetlease.security.CurrentUser;
import com.jetlease.service.BookingService;
import com.jetlease.service.LeaseService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/leases")
public class LeaseController {

    private final LeaseService leaseService;
    private final BookingService bookingService;
    private final AuthContext authContext;

    public LeaseController(LeaseService leaseService, BookingService bookingService, AuthContext authContext) {
        this.leaseService = leaseService;
        this.bookingService = bookingService;
        this.authContext = authContext;
    }

    @GetMapping("/my")
    public List<Lease> my(HttpServletRequest request) {
        CurrentUser user = authContext.requireCustomer(request);
        return leaseService.findByUser(user.email);
    }

    @GetMapping("/{id}")
    public Map<String, Object> get(@PathVariable String id, HttpServletRequest request) {
        CurrentUser user = authContext.requireCustomer(request);
        Lease l = leaseService.findOwnedByUser(id, user.email);
        Booking b = bookingService.findById(l.getBookingId());
        String contract = leaseService.buildLeaseText(l, b);
        return Map.of("lease", l, "contractText", contract);
    }

    @PostMapping("/{id}/sign")
    public Lease sign(@PathVariable String id, @RequestBody SignLeaseRequest req, HttpServletRequest request) {
        CurrentUser user = authContext.requireCustomer(request);
        return leaseService.signLease(user.email, id, req.getLegalName());
    }

    @GetMapping(value = "/{id}/export", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> export(@PathVariable String id, HttpServletRequest request) {
        CurrentUser user = authContext.requireCustomer(request);
        Lease l = leaseService.findOwnedByUser(id, user.email);
        Booking b = bookingService.findById(l.getBookingId());
        String contract = leaseService.buildLeaseText(l, b);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + id + ".txt\"")
                .body(contract);
    }
}
