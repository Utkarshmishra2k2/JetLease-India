package com.jetlease.controller;

import com.jetlease.dto.request.*;
import com.jetlease.dto.response.AuthResponse;
import com.jetlease.dto.response.MessageResponse;
import com.jetlease.entity.User;
import com.jetlease.exception.BadRequestException;
import com.jetlease.security.AuthContext;
import com.jetlease.security.CurrentUser;
import com.jetlease.security.SessionInfo;
import com.jetlease.security.SessionService;
import com.jetlease.service.AuthService;
import com.jetlease.service.MockApiService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final SessionService sessionService;
    private final MockApiService mockApiService;
    private final AuthContext authContext;

    public AuthController(AuthService authService, SessionService sessionService,
                           MockApiService mockApiService, AuthContext authContext) {
        this.authService = authService;
        this.sessionService = sessionService;
        this.mockApiService = mockApiService;
        this.authContext = authContext;
    }

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest req) {
        User user = authService.registerUser(req);
        SessionInfo session = sessionService.createSession(user.getEmail(), user.getRole());
        return new AuthResponse(session.token, user.getEmail(), user.getFullName(), user.getRole());
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest req) {
        User user = authService.loginCustomer(req.getIdentifierType(), req.getIdentifier(), req.getPassword());
        SessionInfo session = sessionService.createSession(user.getEmail(), user.getRole());
        return new AuthResponse(session.token, user.getEmail(), user.getFullName(), user.getRole());
    }

    @PostMapping("/login/otp/request")
    public MessageResponse requestOtp() {
        return new MessageResponse("OTP sent (demo code: 123456).");
    }

    @PostMapping("/admin-login")
    public AuthResponse adminLogin(@RequestBody AdminLoginRequest req) {
        User admin = authService.adminLogin(req.getEmail(), req.getPassword());
        SessionInfo session = sessionService.createSession(admin.getEmail(), admin.getRole());
        return new AuthResponse(session.token, admin.getEmail(), admin.getFullName(), admin.getRole());
    }

    @PostMapping("/logout")
    public MessageResponse logout(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            sessionService.invalidate(header.substring(7).trim());
        }
        return new MessageResponse("Logged out.");
    }

    @GetMapping("/session")
    public AuthResponse session(HttpServletRequest request) {
        CurrentUser user = authContext.requireAuth(request);
        User full = authService.getByEmail(user.email);
        String header = request.getHeader("Authorization").substring(7).trim();
        return new AuthResponse(header, full.getEmail(), full.getFullName(), full.getRole());
    }

    @PostMapping("/forgot-password/request")
    public MessageResponse forgotPasswordRequest(@RequestBody ForgotPasswordRequest req) {
        if (!authService.emailExists(req.getEmail().toLowerCase())) {
            throw new BadRequestException("No account found with that email.");
        }
        return new MessageResponse("Mock OTP sent to " + req.getEmail() + " (use 123456 for this demo).");
    }

    @PostMapping("/forgot-password/confirm")
    public MessageResponse forgotPasswordConfirm(@RequestBody ResetPasswordRequest req) {
        if (!mockApiService.verifyOtp(req.getOtp())) {
            throw new BadRequestException("Incorrect OTP. Use 123456 for this demo.");
        }
        if (req.getNewPassword() == null || req.getNewPassword().length() < 8) {
            throw new BadRequestException("Password must be at least 8 characters.");
        }
        if (!req.getNewPassword().equals(req.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match.");
        }
        authService.updatePassword(req.getEmail().toLowerCase(), req.getNewPassword());
        return new MessageResponse("Password updated successfully.");
    }
}
