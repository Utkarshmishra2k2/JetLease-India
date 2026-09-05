package com.jetlease.controller;

import com.jetlease.dto.request.CreateBookingRequest;
import com.jetlease.dto.request.VerifyAadhaarRequest;
import com.jetlease.dto.request.VerifyLicenseRequest;
import com.jetlease.dto.response.MessageResponse;
import com.jetlease.dto.response.VerifyResult;
import com.jetlease.entity.Booking;
import com.jetlease.entity.Passenger;
import com.jetlease.security.AuthContext;
import com.jetlease.security.CurrentUser;
import com.jetlease.service.BookingService;
import com.jetlease.service.MockApiService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final MockApiService mockApiService;
    private final AuthContext authContext;

    public BookingController(BookingService bookingService, MockApiService mockApiService, AuthContext authContext) {
        this.bookingService = bookingService;
        this.mockApiService = mockApiService;
        this.authContext = authContext;
    }

    @PostMapping("/verify-aadhaar")
    public VerifyResult verifyAadhaar(@RequestBody VerifyAadhaarRequest req, HttpServletRequest request) {
        authContext.requireCustomer(request);
        return mockApiService.verifyAadhaar(req.getAadhaar());
    }

    @PostMapping("/verify-license")
    public VerifyResult verifyLicense(@RequestBody VerifyLicenseRequest req, HttpServletRequest request) {
        authContext.requireCustomer(request);
        return mockApiService.verifyPilotLicense(req.getLicenseNumber());
    }

    @PostMapping
    public Booking create(@RequestBody CreateBookingRequest req, HttpServletRequest request) {
        CurrentUser user = authContext.requireCustomer(request);
        return bookingService.createBooking(user.email, req);
    }

    @GetMapping("/my")
    public List<Booking> my(HttpServletRequest request) {
        CurrentUser user = authContext.requireCustomer(request);
        return bookingService.findByUser(user.email);
    }

    @GetMapping("/{id}")
    public Booking get(@PathVariable String id, HttpServletRequest request) {
        CurrentUser user = authContext.requireCustomer(request);
        return bookingService.findOwnedByUser(id, user.email);
    }

    @GetMapping("/{id}/passengers")
    public List<Passenger> passengers(@PathVariable String id, HttpServletRequest request) {
        CurrentUser user = authContext.requireCustomer(request);
        bookingService.findOwnedByUser(id, user.email);
        return bookingService.findPassengers(id);
    }

    @PostMapping("/{id}/cancel")
    public MessageResponse cancel(@PathVariable String id, HttpServletRequest request) {
        CurrentUser user = authContext.requireCustomer(request);
        bookingService.cancelBooking(user.email, id);
        return new MessageResponse("Booking cancelled.");
    }
}
