package com.jetlease.service;

import com.jetlease.entity.Booking;
import com.jetlease.entity.Lease;
import com.jetlease.entity.Payment;
import com.jetlease.repository.*;
import com.jetlease.util.IdGen;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/** Ported from BookingRules.java - the status machine + resource release logic. */
@Service
public class BookingRulesService {

    public static final Set<String> ENDED_STATUSES = Set.of("Completed", "Cancelled", "Rejected", "Payment Rejected");

    private static final Set<String> PAYABLE_STATUSES = Set.of("Pending Payment", "Payment Rejected");
    private static final Set<String> CANCELLABLE_STATUSES = Set.of(
            "Pending Payment", "Pending Verification", "Lease Pending", "Lease Signed", "Approved");
    private static final Set<String> ACTIVE_STATUSES = Set.of(
            "Lease Pending", "Lease Signed", "Approved");

    private final AuditService auditService;
    private final NotificationService notificationService;
    private final AircraftRepository aircraftRepository;
    private final PilotRepository pilotRepository;
    private final CrewRepository crewRepository;
    private final BookingRepository bookingRepository;
    private final LeaseRepository leaseRepository;

    public BookingRulesService(AuditService auditService, NotificationService notificationService,
                                AircraftRepository aircraftRepository, PilotRepository pilotRepository,
                                CrewRepository crewRepository, BookingRepository bookingRepository,
                                LeaseRepository leaseRepository) {
        this.auditService = auditService;
        this.notificationService = notificationService;
        this.aircraftRepository = aircraftRepository;
        this.pilotRepository = pilotRepository;
        this.crewRepository = crewRepository;
        this.bookingRepository = bookingRepository;
        this.leaseRepository = leaseRepository;
    }

    public void addAudit(String actor, String category, String action, String details) {
        auditService.addAudit(actor, category, action, details);
    }

    public void addNotification(String userEmail, String title, String message, String type) {
        notificationService.addNotification(userEmail, title, message, type);
    }

    public boolean isPayable(String status) {
        return PAYABLE_STATUSES.contains(status);
    }

    public boolean isCancellable(String status) {
        return CANCELLABLE_STATUSES.contains(status);
    }

    public boolean isActive(String status) {
        return ACTIVE_STATUSES.contains(status);
    }

    /** Releases the aircraft, pilot and crew hours reserved for a booking (on cancel/reject). */
    public void releaseBookingResources(String bookingId) {
        Booking b = bookingRepository.findById(bookingId).orElse(null);
        if (b == null) return;

        aircraftRepository.findById(b.getAircraftId()).ifPresent(a -> {
            if ("Booked".equals(a.getStatus())) {
                a.setStatus("Available");
                aircraftRepository.save(a);
            }
        });

        if (b.getAssignedPilotId() != null && !b.getAssignedPilotId().isBlank()) {
            pilotRepository.findById(b.getAssignedPilotId()).ifPresent(p -> {
                p.setRemainingHours(p.getRemainingHours() + b.getHours());
                pilotRepository.save(p);
            });
        }
        if (b.getAssignedCrewIds() != null && !b.getAssignedCrewIds().isBlank()) {
            for (String cid : b.getAssignedCrewIds().split(",")) {
                crewRepository.findById(cid.trim()).ifPresent(c -> {
                    c.setRemainingHours(c.getRemainingHours() + b.getHours());
                    crewRepository.save(c);
                });
            }
        }
    }

    public void voidUnsignedLease(String bookingId) {
        leaseRepository.findByBookingId(bookingId).ifPresent(l -> {
            if (!"Signed".equals(l.getStatus()) && !"Approved".equals(l.getStatus())) {
                l.setStatus("Rejected");
                leaseRepository.save(l);
            }
        });
    }

    public void ensureLeaseForBooking(String bookingId, String userEmail) {
        if (leaseRepository.findByBookingId(bookingId).isPresent()) return;
        Lease l = new Lease();
        l.setId(IdGen.uid("LSE"));
        l.setBookingId(bookingId);
        l.setUserEmail(userEmail);
        l.setStatus("Sent");
        l.setCreatedAt(IdGen.nowIso());
        leaseRepository.save(l);
    }
}
