package com.jetlease.controller;

import com.jetlease.dto.request.ReportIssueRequest;
import com.jetlease.entity.Report;
import com.jetlease.security.AuthContext;
import com.jetlease.security.CurrentUser;
import com.jetlease.service.ReportService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;
    private final AuthContext authContext;

    public ReportController(ReportService reportService, AuthContext authContext) {
        this.reportService = reportService;
        this.authContext = authContext;
    }

    @PostMapping
    public Report file(@RequestBody ReportIssueRequest req, HttpServletRequest request) {
        CurrentUser user = authContext.requireCustomer(request);
        return reportService.fileReport(user.email, req);
    }
}
