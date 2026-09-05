package com.jetlease.service;

import com.jetlease.dto.request.ReportIssueRequest;
import com.jetlease.entity.Booking;
import com.jetlease.entity.Report;
import com.jetlease.exception.BadRequestException;
import com.jetlease.exception.NotFoundException;
import com.jetlease.repository.BookingRepository;
import com.jetlease.repository.ReportRepository;
import com.jetlease.util.IdGen;
import com.jetlease.util.Validators;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class ReportService {

    private static final Set<String> REPORTABLE_STATUSES = Set.of("Dispatched", "Completed");

    private final ReportRepository reportRepository;
    private final BookingRepository bookingRepository;
    private final BookingRulesService bookingRulesService;

    public ReportService(ReportRepository reportRepository, BookingRepository bookingRepository,
                          BookingRulesService bookingRulesService) {
        this.reportRepository = reportRepository;
        this.bookingRepository = bookingRepository;
        this.bookingRulesService = bookingRulesService;
    }

    public Report fileReport(String userEmail, ReportIssueRequest req) {
        Booking b = bookingRepository.findById(req.getBookingId()).orElseThrow(() -> new NotFoundException("Booking not found."));
        if (!b.getUserEmail().equals(userEmail)) throw new BadRequestException("This booking does not belong to you.");
        if (!REPORTABLE_STATUSES.contains(b.getStatus())) {
            throw new BadRequestException("You can only report an issue for a Dispatched or Completed flight.");
        }
        if (req.getSubject() == null || req.getSubject().trim().isEmpty()) {
            throw new BadRequestException("Subject is required.");
        }
        String detailsErr = Validators.message(req.getDetails());
        if (!detailsErr.isEmpty()) throw new BadRequestException(detailsErr);

        Report r = new Report();
        r.setId(IdGen.uid("RPT"));
        r.setBookingId(req.getBookingId());
        r.setUserEmail(userEmail);
        r.setSubject(req.getSubject());
        r.setDetails(req.getDetails());
        r.setStatus("Open");
        r.setCreatedAt(IdGen.nowIso());
        reportRepository.save(r);

        bookingRulesService.addNotification("admin", "New Issue Report",
                "Report filed for booking " + req.getBookingId() + " by " + userEmail, "warning");
        return r;
    }

    public List<Report> findAll() {
        return reportRepository.findAllByOrderByCreatedAtDesc();
    }
}
