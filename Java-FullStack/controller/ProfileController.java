package com.jetlease.controller;

import com.jetlease.dto.request.ChangePhoneRequest;
import com.jetlease.dto.request.UpdateProfileRequest;
import com.jetlease.entity.User;
import com.jetlease.security.AuthContext;
import com.jetlease.security.CurrentUser;
import com.jetlease.service.AuthService;
import com.jetlease.service.ProfileService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;
    private final AuthService authService;
    private final AuthContext authContext;

    public ProfileController(ProfileService profileService, AuthService authService, AuthContext authContext) {
        this.profileService = profileService;
        this.authService = authService;
        this.authContext = authContext;
    }

    @GetMapping
    public User get(HttpServletRequest request) {
        CurrentUser user = authContext.requireCustomer(request);
        return authService.getByEmail(user.email);
    }

    @PutMapping
    public User update(@RequestBody UpdateProfileRequest req, HttpServletRequest request) {
        CurrentUser user = authContext.requireCustomer(request);
        return profileService.updateProfile(user.email, req);
    }

    @PutMapping("/phone")
    public User changePhone(@RequestBody ChangePhoneRequest req, HttpServletRequest request) {
        CurrentUser user = authContext.requireCustomer(request);
        return profileService.changePhone(user.email, req);
    }
}
