package com.jetlease.service;

import com.jetlease.entity.Booking;
import com.jetlease.entity.Lease;
import com.jetlease.exception.BadRequestException;
import com.jetlease.exception.ForbiddenException;
import com.jetlease.exception.NotFoundException;
import com.jetlease.repository.BookingRepository;
import com.jetlease.repository.LeaseRepository;
import com.jetlease.util.IdGen;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LeaseService {

    private final LeaseRepository leaseRepository;
    private final BookingRepository bookingRepository;
    private final BookingRulesService bookingRulesService;

    public LeaseService(LeaseRepository leaseRepository, BookingRepository bookingRepository,
                         BookingRulesService bookingRulesService) {
        this.leaseRepository = leaseRepository;
        this.bookingRepository = bookingRepository;
        this.bookingRulesService = bookingRulesService;
    }

    public List<Lease> findByUser(String userEmail) {
        return leaseRepository.findByUserEmailOrderByCreatedAtDesc(userEmail);
    }

    public List<Lease> findAll() {
        return leaseRepository.findAllByOrderByCreatedAtDesc();
    }

    public Lease findById(String id) {
        return leaseRepository.findById(id).orElseThrow(() -> new NotFoundException("Lease not found."));
    }

    public Lease findOwnedByUser(String id, String userEmail) {
        Lease l = findById(id);
        if (!l.getUserEmail().equals(userEmail)) throw new ForbiddenException("This lease does not belong to you.");
        return l;
    }

    public String buildLeaseText(Lease l, Booking b) {
        String route = b.getOrigin() + " to " + b.getDestination();
        StringBuilder sb = new StringBuilder();
        sb.append("AIRCRAFT LEASE AGREEMENT\n");
        sb.append("Lease ID: ").append(l.getId()).append("\n");
        sb.append("Booking Reference: ").append(b.getId()).append("\n");
        sb.append("Lessee: ").append(l.getUserEmail()).append("\n");
        sb.append("Aircraft: ").append(b.getAircraftModel()).append("\n");
        sb.append("Route: ").append(route).append("\n");
        sb.append("Date of Flight: ").append(b.getDate()).append("\n");
        sb.append("Total Charter Value: INR ").append(b.getTotal()).append("\n\n");
        sb.append("This agreement confirms the terms under which JetLease India Charters Pvt Ltd\n");
        sb.append("leases the above aircraft to the lessee for the stated route and date, subject\n");
        sb.append("to all applicable DGCA regulations and the JetLease Terms of Service.\n\n");
        sb.append("Status: ").append(l.getStatus()).append("\n");
        if (l.getSignedBy() != null) sb.append("Signed By: ").append(l.getSignedBy()).append(" on ").append(l.getSignedDate()).append("\n");
        return sb.toString();
    }

    public Lease signLease(String userEmail, String leaseId, String legalName) {
        Lease l = findOwnedByUser(leaseId, userEmail);
        if (!"Sent".equals(l.getStatus())) {
            throw new BadRequestException("Only a lease with status \"Sent\" can be signed.");
        }
        if (legalName == null || legalName.trim().length() < 3) {
            throw new BadRequestException("Enter your full legal name.");
        }
        String today = IdGen.todayIso();
        l.setStatus("Signed");
        l.setSignedBy(legalName.trim());
        l.setSignedDate(today);
        leaseRepository.save(l);

        Booking b = bookingRepository.findById(l.getBookingId()).orElseThrow(() -> new NotFoundException("Booking not found."));
        b.setStatus("Lease Signed");
        bookingRepository.save(b);

        bookingRulesService.addAudit(userEmail, "Lease", "Lease Signed", leaseId);
        bookingRulesService.addNotification("admin", "Lease Signed", "Lease " + leaseId + " was signed by " + userEmail + ". Awaiting approval.", "info");
        bookingRulesService.addNotification(userEmail, "Lease Signed", "You signed lease " + leaseId + ". It is now awaiting admin approval.", "success");
        return l;
    }
}
