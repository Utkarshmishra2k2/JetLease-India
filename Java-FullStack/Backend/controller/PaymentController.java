package com.jetlease.controller;

import com.jetlease.dto.request.PayRequest;
import com.jetlease.entity.Payment;
import com.jetlease.security.AuthContext;
import com.jetlease.security.CurrentUser;
import com.jetlease.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final AuthContext authContext;

    public PaymentController(PaymentService paymentService, AuthContext authContext) {
        this.paymentService = paymentService;
        this.authContext = authContext;
    }

    @GetMapping("/my")
    public List<Payment> my(HttpServletRequest request) {
        CurrentUser user = authContext.requireCustomer(request);
        return paymentService.findByUser(user.email);
    }

    @GetMapping("/booking/{bookingId}")
    public Object payable(@PathVariable String bookingId, HttpServletRequest request) {
        CurrentUser user = authContext.requireCustomer(request);
        return paymentService.getPayableBooking(user.email, bookingId);
    }

    @PostMapping("/booking/{bookingId}/pay")
    public Payment pay(@PathVariable String bookingId, @RequestBody PayRequest req, HttpServletRequest request) {
        CurrentUser user = authContext.requireCustomer(request);
        return paymentService.submitPayment(user.email, bookingId, req.getTransactionId());
    }
}
