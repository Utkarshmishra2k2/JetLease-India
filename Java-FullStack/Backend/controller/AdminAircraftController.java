package com.jetlease.controller;

import com.jetlease.dto.request.AddAircraftRequest;
import com.jetlease.dto.request.RateRequest;
import com.jetlease.dto.request.StatusRequest;
import com.jetlease.dto.response.MessageResponse;
import com.jetlease.entity.Aircraft;
import com.jetlease.security.AuthContext;
import com.jetlease.security.CurrentUser;
import com.jetlease.service.AdminService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/aircraft")
public class AdminAircraftController {

    private final AdminService adminService;
    private final AuthContext authContext;

    public AdminAircraftController(AdminService adminService, AuthContext authContext) {
        this.adminService = adminService;
        this.authContext = authContext;
    }

    @GetMapping
    public List<Aircraft> all(HttpServletRequest request) {
        authContext.requireAdmin(request);
        return adminService.allAircraft();
    }

    @PostMapping
    public Aircraft add(@RequestBody AddAircraftRequest req, HttpServletRequest request) {
        CurrentUser admin = authContext.requireAdmin(request);
        return adminService.addAircraft(admin.email, req);
    }

    @PutMapping("/{id}/status")
    public Aircraft updateStatus(@PathVariable String id, @RequestBody StatusRequest req, HttpServletRequest request) {
        CurrentUser admin = authContext.requireAdmin(request);
        return adminService.updateAircraftStatus(admin.email, id, req.getStatus());
    }

    @PutMapping("/{id}/rate")
    public Aircraft updateRate(@PathVariable String id, @RequestBody RateRequest req, HttpServletRequest request) {
        CurrentUser admin = authContext.requireAdmin(request);
        return adminService.updateAircraftRate(admin.email, id, req.getHourlyRate());
    }

    @DeleteMapping("/{id}")
    public MessageResponse delete(@PathVariable String id, HttpServletRequest request) {
        CurrentUser admin = authContext.requireAdmin(request);
        adminService.deleteAircraft(admin.email, id);
        return new MessageResponse("Aircraft deleted.");
    }
}
