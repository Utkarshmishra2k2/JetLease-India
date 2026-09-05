package com.jetlease.controller;

import com.jetlease.dto.response.MessageResponse;
import com.jetlease.entity.Notification;
import com.jetlease.security.AuthContext;
import com.jetlease.security.CurrentUser;
import com.jetlease.service.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final AuthContext authContext;

    public NotificationController(NotificationService notificationService, AuthContext authContext) {
        this.notificationService = notificationService;
        this.authContext = authContext;
    }

    @GetMapping("/my")
    public List<Notification> my(HttpServletRequest request) {
        CurrentUser user = authContext.requireCustomer(request);
        return notificationService.findByUser(user.email);
    }

    @PostMapping("/mark-read")
    public MessageResponse markRead(HttpServletRequest request) {
        CurrentUser user = authContext.requireCustomer(request);
        notificationService.markAllRead(user.email);
        return new MessageResponse("All notifications marked as read.");
    }
}
