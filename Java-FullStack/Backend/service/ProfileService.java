package com.jetlease.service;

import com.jetlease.dto.request.ChangePhoneRequest;
import com.jetlease.dto.request.UpdateProfileRequest;
import com.jetlease.entity.User;
import com.jetlease.exception.BadRequestException;
import com.jetlease.repository.UserRepository;
import com.jetlease.util.Validators;
import org.springframework.stereotype.Service;

@Service
public class ProfileService {

    private final UserRepository userRepository;
    private final BookingRulesService bookingRulesService;
    private final MockApiService mockApiService;

    public ProfileService(UserRepository userRepository, BookingRulesService bookingRulesService,
                           MockApiService mockApiService) {
        this.userRepository = userRepository;
        this.bookingRulesService = bookingRulesService;
        this.mockApiService = mockApiService;
    }

    public User updateProfile(String email, UpdateProfileRequest req) {
        String nameErr = Validators.name(req.getFullName());
        if (!nameErr.isEmpty()) throw new BadRequestException(nameErr);
        String dobErr = Validators.dob(req.getDob());
        if (!dobErr.isEmpty()) throw new BadRequestException(dobErr);
        if (!Validators.isAdult(req.getDob())) throw new BadRequestException("You must be 18 or older.");
        String contactErr = Validators.phone10(req.getEmergencyContact());
        if (!contactErr.isEmpty()) throw new BadRequestException(contactErr);

        User user = userRepository.findByEmail(email).orElseThrow();
        user.setFullName(req.getFullName());
        user.setDob(req.getDob());
        user.setEmergencyContact(req.getEmergencyContact());
        userRepository.save(user);

        bookingRulesService.addAudit(email, "Login", "Profile Updated", "");
        return user;
    }

    public User changePhone(String email, ChangePhoneRequest req) {
        String phoneErr = Validators.phone10(req.getNewPhone());
        if (!phoneErr.isEmpty()) throw new BadRequestException(phoneErr);
        if (!mockApiService.verifyOtp(req.getOtp())) {
            throw new BadRequestException("Incorrect OTP. Phone number not changed. (Use 123456 for this demo.)");
        }
        User user = userRepository.findByEmail(email).orElseThrow();
        user.setPhone(req.getNewPhone());
        userRepository.save(user);

        bookingRulesService.addAudit(email, "Login", "Phone Number Changed", req.getNewPhone());
        return user;
    }
}
