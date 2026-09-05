package com.jetlease.service;

import com.jetlease.dto.request.RegisterRequest;
import com.jetlease.entity.User;
import com.jetlease.exception.BadRequestException;
import com.jetlease.exception.NotFoundException;
import com.jetlease.exception.UnauthorizedException;
import com.jetlease.repository.UserRepository;
import com.jetlease.util.IdGen;
import com.jetlease.util.Validators;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final BookingRulesService bookingRulesService;

    public AuthService(UserRepository userRepository, BookingRulesService bookingRulesService) {
        this.userRepository = userRepository;
        this.bookingRulesService = bookingRulesService;
    }

    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email.toLowerCase());
    }

    public User registerUser(RegisterRequest req) {
        String email = req.getEmail().toLowerCase();

        validate("Full Name", Validators.name(req.getFullName()));
        validate("Email", Validators.email(req.getEmail()));
        if (emailExists(email)) throw new BadRequestException("An account with this email already exists.");
        validate("Phone", Validators.phone10(req.getPhone()));

        String dobErr = Validators.dob(req.getDob());
        if (!dobErr.isEmpty()) throw new BadRequestException(dobErr);
        if (!Validators.isAdult(req.getDob())) throw new BadRequestException("You must be 18 or older.");

        validate("Emergency Contact", Validators.phone10(req.getEmergencyContact()));

        if (req.getPassword() == null || req.getPassword().length() < 8) {
            throw new BadRequestException("Password must be at least 8 characters.");
        }
        if (!req.getPassword().equals(req.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match.");
        }

        User user = new User();
        user.setId(IdGen.uid("CUS"));
        user.setFullName(req.getFullName().trim());
        user.setEmail(email);
        user.setPhone(req.getPhone());
        user.setDob(req.getDob());
        user.setEmergencyContact(req.getEmergencyContact());
        user.setPassword(req.getPassword());
        user.setCountry("India");
        user.setRole("customer");
        user.setStatus("active");
        user.setMembership("none");
        user.setLoyaltyPoints(0);
        user.setCreatedAt(IdGen.nowIso());
        userRepository.save(user);

        bookingRulesService.addAudit(email, "Login", "Account Registered", "Self-registered");
        bookingRulesService.addNotification(email, "Welcome to JetLease",
                "Your account has been created. Explore the fleet and book your first flight.", "success");
        return user;
    }

    public User loginCustomer(String identifierType, String identifier, String password) {
        User user;
        if ("phone".equals(identifierType)) {
            user = userRepository.findByPhoneAndPasswordAndRole(identifier, password, "customer").orElse(null);
        } else {
            user = userRepository.findByEmailAndPasswordAndRole(identifier.toLowerCase(), password, "customer").orElse(null);
        }
        if (user == null) {
            throw new UnauthorizedException("Invalid credentials. Try demo@jetlease.in / Demo@123");
        }
        if ("suspended".equals(user.getStatus())) {
            throw new UnauthorizedException("This account has been suspended. Contact support.");
        }
        bookingRulesService.addAudit(user.getEmail(), "Login", "Customer Login", "");
        return user;
    }

    public User adminLogin(String email, String password) {
        User admin = userRepository.findByEmailAndPasswordAndRole(email.toLowerCase(), password, "admin").orElse(null);
        if (admin == null) throw new UnauthorizedException("Invalid admin credentials.");
        bookingRulesService.addAudit(admin.getEmail(), "Login", "Admin Login", "");
        return admin;
    }

    public User getByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new NotFoundException("User not found."));
    }

    public void updatePassword(String email, String newPassword) {
        User user = getByEmail(email);
        user.setPassword(newPassword);
        userRepository.save(user);
        bookingRulesService.addAudit(email, "Login", "Password Reset", "");
    }

    private void validate(String field, String error) {
        if (!error.isEmpty()) throw new BadRequestException(error);
    }
}
