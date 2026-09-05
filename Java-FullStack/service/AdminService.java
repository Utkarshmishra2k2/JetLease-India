package com.jetlease.service;

import com.jetlease.dto.request.AddAircraftRequest;
import com.jetlease.dto.request.AssignCrewRequest;
import com.jetlease.entity.*;
import com.jetlease.exception.BadRequestException;
import com.jetlease.exception.NotFoundException;
import com.jetlease.repository.*;
import com.jetlease.util.IdGen;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Ported from AdminController.java - fleet, bookings, payments, leases, customers, crew, routes, inbox, exports, audit. */
@Service
public class AdminService {

    private final AircraftRepository aircraftRepository;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final LeaseRepository leaseRepository;
    private final UserRepository userRepository;
    private final PilotRepository pilotRepository;
    private final CrewRepository crewRepository;
    private final RouteRepository routeRepository;
    private final ContactMessageRepository contactMessageRepository;
    private final ReportRepository reportRepository;
    private final MockApiService mockApiService;
    private final BookingRulesService bookingRulesService;

    public AdminService(AircraftRepository aircraftRepository, BookingRepository bookingRepository,
                         PaymentRepository paymentRepository, LeaseRepository leaseRepository,
                         UserRepository userRepository, PilotRepository pilotRepository,
                         CrewRepository crewRepository, RouteRepository routeRepository,
                         ContactMessageRepository contactMessageRepository, ReportRepository reportRepository,
                         MockApiService mockApiService, BookingRulesService bookingRulesService) {
        this.aircraftRepository = aircraftRepository;
        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
        this.leaseRepository = leaseRepository;
        this.userRepository = userRepository;
        this.pilotRepository = pilotRepository;
        this.crewRepository = crewRepository;
        this.routeRepository = routeRepository;
        this.contactMessageRepository = contactMessageRepository;
        this.reportRepository = reportRepository;
        this.mockApiService = mockApiService;
        this.bookingRulesService = bookingRulesService;
    }

    // ---------- Overview ----------
    public Map<String, Object> overview() {
        Map<String, Object> stats = new LinkedHashMap<>();
        List<Booking> bookings = bookingRepository.findAll();
        stats.put("totalBookings", bookings.size());

        Map<String, Long> byStatus = new LinkedHashMap<>();
        bookings.forEach(b -> byStatus.merge(b.getStatus(), 1L, Long::sum));
        stats.put("bookingsByStatus", byStatus);

        long revenue = bookings.stream().filter(b -> "Completed".equals(b.getStatus())).mapToLong(Booking::getTotal).sum();
        stats.put("revenueCompleted", revenue);

        Map<String, Long> aircraftByStatus = new LinkedHashMap<>();
        aircraftRepository.findAll().forEach(a -> aircraftByStatus.merge(a.getStatus(), 1L, Long::sum));
        stats.put("aircraftByStatus", aircraftByStatus);

        Map<String, Long> popular = new LinkedHashMap<>();
        bookings.forEach(b -> popular.merge(b.getAircraftModel(), 1L, Long::sum));
        stats.put("popularAircraft", popular.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(3)
                .collect(LinkedHashMap::new, (m, e) -> m.put(e.getKey(), e.getValue()), Map::putAll));
        return stats;
    }

    // ---------- Aircraft ----------
    public List<Aircraft> allAircraft() {
        return aircraftRepository.findAll();
    }

    public Aircraft addAircraft(String adminEmail, AddAircraftRequest req) {
        Aircraft a = new Aircraft();
        a.setId(IdGen.uid("AC"));
        a.setReg(req.getReg());
        a.setModel(req.getModel());
        a.setManufacturer(req.getManufacturer());
        a.setCategory(req.getCategory());
        a.setCapacity(req.getCapacity());
        a.setSpeed(req.getSpeed());
        a.setRangeKm(req.getRangeKm());
        a.setHourlyRate(req.getHourlyRate());
        a.setStatus("Available");
        a.setTypeRating(req.getTypeRating());
        aircraftRepository.save(a);
        bookingRulesService.addAudit(adminEmail, "Admin", "Aircraft Added", a.getId() + " - " + req.getModel());
        return a;
    }

    public Aircraft updateAircraftStatus(String adminEmail, String id, String status) {
        Aircraft a = aircraftRepository.findById(id).orElseThrow(() -> new NotFoundException("Aircraft not found."));
        a.setStatus(status);
        aircraftRepository.save(a);
        bookingRulesService.addAudit(adminEmail, "Admin", "Aircraft Status Changed", id + " -> " + status);
        return a;
    }

    public Aircraft updateAircraftRate(String adminEmail, String id, long rate) {
        Aircraft a = aircraftRepository.findById(id).orElseThrow(() -> new NotFoundException("Aircraft not found."));
        a.setHourlyRate(rate);
        aircraftRepository.save(a);
        bookingRulesService.addAudit(adminEmail, "Admin", "Aircraft Rate Changed", id + " -> " + rate);
        return a;
    }

    public void deleteAircraft(String adminEmail, String id) {
        Aircraft a = aircraftRepository.findById(id).orElseThrow(() -> new NotFoundException("Aircraft not found."));
        if ("Booked".equals(a.getStatus())) throw new BadRequestException("Cannot delete an aircraft that is currently booked.");
        aircraftRepository.deleteById(id);
        bookingRulesService.addAudit(adminEmail, "Admin", "Aircraft Deleted", id + " - " + a.getModel());
    }

    // ---------- Bookings ----------
    public List<Booking> allBookings() {
        return bookingRepository.findAllByOrderByCreatedAtDesc();
    }

    public Booking getBooking(String id) {
        return bookingRepository.findById(id).orElseThrow(() -> new NotFoundException("Booking not found."));
    }

    @Transactional
    public Booking assignCrew(String adminEmail, String bookingId, AssignCrewRequest req) {
        Booking b = getBooking(bookingId);
        double hours = b.getHours();

        Pilot pilot = pilotRepository.findById(req.getPilotId()).orElseThrow(() -> new NotFoundException("Pilot not found."));
        if (!pilot.isAvailable() || pilot.getRemainingHours() < hours) {
            throw new BadRequestException("This pilot does not have enough remaining hours for this flight.");
        }
        if (req.getCrewIds() == null || req.getCrewIds().isEmpty()) {
            throw new BadRequestException("At least one crew member must be selected.");
        }
        List<Crew> crewList = req.getCrewIds().stream()
                .map(cid -> crewRepository.findById(cid).orElseThrow(() -> new NotFoundException("Crew member not found: " + cid)))
                .toList();
        for (Crew c : crewList) {
            if (!c.isAvailable() || c.getRemainingHours() < hours) {
                throw new BadRequestException("Crew member " + c.getId() + " does not have enough remaining hours.");
            }
        }

        pilot.setRemainingHours(pilot.getRemainingHours() - hours);
        pilotRepository.save(pilot);
        for (Crew c : crewList) {
            c.setRemainingHours(c.getRemainingHours() - hours);
            crewRepository.save(c);
        }

        b.setAssignedPilotId(pilot.getId());
        b.setAssignedCrewIds(String.join(",", req.getCrewIds()));
        bookingRepository.save(b);

        bookingRulesService.addAudit(adminEmail, "Admin", "Crew Assigned",
                bookingId + " pilot=" + pilot.getId() + " crew=" + String.join(",", req.getCrewIds()));
        return b;
    }

    public Booking advanceBooking(String adminEmail, String bookingId, String newStatus) {
        Booking b = getBooking(bookingId);
        b.setStatus(newStatus);
        bookingRepository.save(b);
        bookingRulesService.addAudit(adminEmail, "Admin", "Booking Advanced", bookingId + " -> " + newStatus);
        bookingRulesService.addNotification(b.getUserEmail(), "Booking Update",
                "Your booking " + bookingId + " is now \"" + newStatus + "\".", "info");
        return b;
    }

    public Booking completeBooking(String adminEmail, String bookingId) {
        Booking b = advanceBooking(adminEmail, bookingId, "Completed");
        int points = (int) Math.round(b.getTotal() / 10000.0);
        userRepository.findByEmail(b.getUserEmail()).ifPresent(u -> {
            u.setLoyaltyPoints(u.getLoyaltyPoints() + points);
            userRepository.save(u);
        });
        bookingRulesService.addNotification(b.getUserEmail(), "Loyalty Points Earned",
                "You earned " + points + " loyalty points for booking " + bookingId + ".", "success");
        return b;
    }

    @Transactional
    public Booking rejectBooking(String adminEmail, String bookingId) {
        Booking b = getBooking(bookingId);
        paymentRepository.findByBookingId(bookingId).filter(p -> "VERIFIED".equals(p.getStatus())).ifPresent(p -> {
            p.setRefundAmount(p.getAmount());
            p.setCancellationFee(0);
            p.setStatus("RETURNED");
            paymentRepository.save(p);
        });
        bookingRulesService.releaseBookingResources(bookingId);
        bookingRulesService.voidUnsignedLease(bookingId);

        b.setStatus("Rejected");
        b.setAssignedPilotId(null);
        b.setAssignedCrewIds(null);
        bookingRepository.save(b);

        bookingRulesService.addAudit(adminEmail, "Admin", "Booking Rejected", bookingId);
        bookingRulesService.addNotification(b.getUserEmail(), "Booking Rejected",
                "Booking " + bookingId + " was rejected by our team.", "warning");
        return b;
    }

    // ---------- Payments ----------
    public List<Payment> allPayments() {
        return paymentRepository.findAllByOrderBySubmittedAtDesc();
    }

    public Map<String, Object> checkPaymentLedger(String paymentId) {
        Payment p = paymentRepository.findById(paymentId).orElseThrow(() -> new NotFoundException("Payment not found."));
        var result = mockApiService.verifyPaymentAgainstLedger(p.getTransactionId(), p.getBookingId(), p.getAmount());
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("verified", result.verified);
        map.put("message", result.message);
        return map;
    }

    @Transactional
    public Payment verifyPayment(String adminEmail, String paymentId) {
        Payment p = paymentRepository.findById(paymentId).orElseThrow(() -> new NotFoundException("Payment not found."));
        if (!"PENDING_VERIFICATION".equals(p.getStatus())) {
            throw new BadRequestException("This payment is already \"" + p.getStatus() + "\" - nothing to verify.");
        }
        p.setStatus("VERIFIED");
        paymentRepository.save(p);

        Booking b = bookingRepository.findById(p.getBookingId()).orElseThrow(() -> new NotFoundException("Booking not found."));
        b.setStatus("Lease Pending");
        bookingRepository.save(b);

        bookingRulesService.ensureLeaseForBooking(p.getBookingId(), p.getUserEmail());
        bookingRulesService.addAudit(adminEmail, "Admin", "Payment Verified", paymentId);
        bookingRulesService.addNotification(p.getUserEmail(), "Payment Verified",
                "Your payment for " + p.getBookingId() + " has been verified. Your lease is ready.", "success");
        return p;
    }

    @Transactional
    public Payment rejectPayment(String adminEmail, String paymentId) {
        Payment p = paymentRepository.findById(paymentId).orElseThrow(() -> new NotFoundException("Payment not found."));
        if (!"PENDING_VERIFICATION".equals(p.getStatus())) {
            throw new BadRequestException("This payment is already \"" + p.getStatus() + "\" - nothing to reject.");
        }
        p.setStatus("REJECTED");
        paymentRepository.save(p);

        Booking b = bookingRepository.findById(p.getBookingId()).orElseThrow(() -> new NotFoundException("Booking not found."));
        b.setStatus("Payment Rejected");
        bookingRepository.save(b);

        bookingRulesService.addAudit(adminEmail, "Admin", "Payment Rejected", paymentId);
        bookingRulesService.addNotification(p.getUserEmail(), "Payment Rejected",
                "Your payment for " + p.getBookingId() + " was rejected. Please resubmit.", "warning");
        return p;
    }

    // ---------- Leases ----------
    public List<Lease> allLeases() {
        return leaseRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public Lease approveLease(String adminEmail, String leaseId) {
        Lease l = leaseRepository.findById(leaseId).orElseThrow(() -> new NotFoundException("Lease not found."));
        if (!"Signed".equals(l.getStatus())) {
            throw new BadRequestException("Only a lease with status \"Signed\" can be approved here. Current status: " + l.getStatus());
        }
        l.setStatus("Approved");
        l.setApprovalDate(IdGen.todayIso());
        leaseRepository.save(l);

        Booking b = bookingRepository.findById(l.getBookingId()).orElseThrow(() -> new NotFoundException("Booking not found."));
        b.setStatus("Approved");
        bookingRepository.save(b);

        bookingRulesService.addAudit(adminEmail, "Admin", "Lease Approved", leaseId);
        bookingRulesService.addNotification(l.getUserEmail(), "Lease Approved",
                "Your lease for booking " + l.getBookingId() + " has been approved.", "success");
        return l;
    }

    @Transactional
    public Lease rejectLease(String adminEmail, String leaseId) {
        Lease l = leaseRepository.findById(leaseId).orElseThrow(() -> new NotFoundException("Lease not found."));
        if (!"Signed".equals(l.getStatus())) {
            throw new BadRequestException("Only a lease with status \"Signed\" can be rejected here. Current status: " + l.getStatus());
        }
        l.setStatus("Rejected");
        leaseRepository.save(l);

        paymentRepository.findByBookingId(l.getBookingId()).filter(p -> "VERIFIED".equals(p.getStatus())).ifPresent(p -> {
            p.setRefundAmount(p.getAmount());
            p.setCancellationFee(0);
            p.setStatus("RETURNED");
            paymentRepository.save(p);
        });
        bookingRulesService.releaseBookingResources(l.getBookingId());

        Booking b = bookingRepository.findById(l.getBookingId()).orElseThrow(() -> new NotFoundException("Booking not found."));
        b.setStatus("Rejected");
        b.setAssignedPilotId(null);
        b.setAssignedCrewIds(null);
        bookingRepository.save(b);

        bookingRulesService.addAudit(adminEmail, "Admin", "Lease Rejected", leaseId);
        bookingRulesService.addNotification(l.getUserEmail(), "Lease Rejected",
                "Your lease for booking " + l.getBookingId() + " was rejected and your payment is being fully refunded.", "warning");
        return l;
    }

    // ---------- Customers ----------
    public List<User> allCustomers() {
        return userRepository.findByRole("customer");
    }

    public User toggleCustomerStatus(String adminEmail, String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new NotFoundException("Customer not found."));
        String newStatus = "suspended".equals(user.getStatus()) ? "active" : "suspended";
        user.setStatus(newStatus);
        userRepository.save(user);

        bookingRulesService.addAudit(adminEmail, "Admin",
                "Customer " + ("suspended".equals(newStatus) ? "Suspended" : "Reactivated"), email);
        bookingRulesService.addNotification(email, "Account " + ("suspended".equals(newStatus) ? "Suspended" : "Reactivated"),
                "Your account has been " + newStatus + " by the JetLease team.", "warning");
        return user;
    }

    // ---------- Crew / Pilots ----------
    public List<Pilot> allPilots() {
        return pilotRepository.findAll();
    }

    public List<Crew> allCrew() {
        return crewRepository.findAll();
    }

    public Pilot togglePilotAvailability(String adminEmail, String id) {
        Pilot p = pilotRepository.findById(id).orElseThrow(() -> new NotFoundException("Pilot not found."));
        p.setAvailable(!p.isAvailable());
        pilotRepository.save(p);
        bookingRulesService.addAudit(adminEmail, "Admin", "Pilot Availability Toggled", id);
        return p;
    }

    public Crew toggleCrewAvailability(String adminEmail, String id) {
        Crew c = crewRepository.findById(id).orElseThrow(() -> new NotFoundException("Crew member not found."));
        c.setAvailable(!c.isAvailable());
        crewRepository.save(c);
        bookingRulesService.addAudit(adminEmail, "Admin", "Crew Availability Toggled", id);
        return c;
    }

    // ---------- Routes ----------
    public List<Map<String, Object>> routesWithBookingCounts() {
        List<Route> routes = routeRepository.findAll();
        return routes.stream().map(r -> {
            long count = bookingRepository.findByOriginOrDestination(r.getCode(), r.getCode()).size();
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("code", r.getCode());
            map.put("city", r.getCity());
            map.put("airport", r.getAirport());
            map.put("bookingCount", count);
            return map;
        }).toList();
    }

    // ---------- Inbox ----------
    public List<ContactMessage> allContactMessages() {
        return contactMessageRepository.findAllByOrderByCreatedAtDesc();
    }

    public ContactMessage markMessageRead(String adminEmail, String id) {
        ContactMessage msg = contactMessageRepository.findById(id).orElseThrow(() -> new NotFoundException("Message not found."));
        msg.setStatus("Read");
        contactMessageRepository.save(msg);
        bookingRulesService.addAudit(adminEmail, "Admin", "Contact Message Read", id);
        return msg;
    }

    public List<Report> allReports() {
        return reportRepository.findAllByOrderByCreatedAtDesc();
    }

    public Report resolveReport(String adminEmail, String id) {
        Report r = reportRepository.findById(id).orElseThrow(() -> new NotFoundException("Report not found."));
        r.setStatus("Resolved");
        reportRepository.save(r);
        bookingRulesService.addAudit(adminEmail, "Admin", "Issue Report Resolved", id);
        bookingRulesService.addNotification(r.getUserEmail(), "Issue Resolved",
                "Your reported issue for booking " + r.getBookingId() + " has been resolved.", "success");
        return r;
    }

    // ---------- Exports ----------
    public String exportBookingsCsv() {
        StringBuilder sb = new StringBuilder("id,user_email,type,trip_type,origin,destination,date,status,total\n");
        for (Booking b : bookingRepository.findAll()) {
            sb.append(csvRow(b.getId(), b.getUserEmail(), b.getType(), b.getTripType(), b.getOrigin(),
                    b.getDestination(), b.getDate(), b.getStatus(), String.valueOf(b.getTotal())));
        }
        return sb.toString();
    }

    public String exportCustomersCsv() {
        StringBuilder sb = new StringBuilder("id,full_name,email,phone,status,membership,loyalty_points\n");
        for (User u : userRepository.findByRole("customer")) {
            sb.append(csvRow(u.getId(), u.getFullName(), u.getEmail(), u.getPhone(), u.getStatus(),
                    u.getMembership(), String.valueOf(u.getLoyaltyPoints())));
        }
        return sb.toString();
    }

    public String exportPaymentsCsv() {
        StringBuilder sb = new StringBuilder("id,booking_id,user_email,amount,transaction_id,status\n");
        for (Payment p : paymentRepository.findAll()) {
            sb.append(csvRow(p.getId(), p.getBookingId(), p.getUserEmail(), String.valueOf(p.getAmount()),
                    p.getTransactionId(), p.getStatus()));
        }
        return sb.toString();
    }

    private String csvRow(String... cols) {
        StringBuilder row = new StringBuilder();
        for (int i = 0; i < cols.length; i++) {
            if (i > 0) row.append(",");
            String v = cols[i];
            row.append(v == null ? "" : v.replace(",", ";"));
        }
        return row.append("\n").toString();
    }
}
