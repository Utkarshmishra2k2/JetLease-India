package com.jetlease.controller;

import com.jetlease.entity.Crew;
import com.jetlease.entity.Pilot;
import com.jetlease.security.AuthContext;
import com.jetlease.security.CurrentUser;
import com.jetlease.service.AdminService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminCrewController {

    private final AdminService adminService;
    private final AuthContext authContext;

    public AdminCrewController(AdminService adminService, AuthContext authContext) {
        this.adminService = adminService;
        this.authContext = authContext;
    }

    @GetMapping("/pilots")
    public List<Pilot> pilots(HttpServletRequest request) {
        authContext.requireAdmin(request);
        return adminService.allPilots();
    }

    @GetMapping("/crew")
    public List<Crew> crew(HttpServletRequest request) {
        authContext.requireAdmin(request);
        return adminService.allCrew();
    }

    @PostMapping("/pilots/{id}/toggle-availability")
    public Pilot togglePilot(@PathVariable String id, HttpServletRequest request) {
        CurrentUser admin = authContext.requireAdmin(request);
        return adminService.togglePilotAvailability(admin.email, id);
    }

    @PostMapping("/crew/{id}/toggle-availability")
    public Crew toggleCrew(@PathVariable String id, HttpServletRequest request) {
        CurrentUser admin = authContext.requireAdmin(request);
        return adminService.toggleCrewAvailability(admin.email, id);
    }
}
