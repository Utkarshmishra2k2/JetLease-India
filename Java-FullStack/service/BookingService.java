package com.jetlease.service;

import com.jetlease.dto.request.CreateBookingRequest;
import com.jetlease.dto.request.PassengerRequest;
import com.jetlease.dto.response.CostBreakdown;
import com.jetlease.entity.Aircraft;
import com.jetlease.entity.Booking;
import com.jetlease.entity.Passenger;
import com.jetlease.exception.BadRequestException;
import com.jetlease.exception.ForbiddenException;
import com.jetlease.exception.NotFoundException;
import com.jetlease.repository.AircraftRepository;
import com.jetlease.repository.BookingRepository;
import com.jetlease.repository.PassengerRepository;
import com.jetlease.repository.PaymentRepository;
import com.jetlease.repository.RouteRepository;
import com.jetlease.util.IdGen;
import com.jetlease.util.Validators;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Ported from BookingController.startNewBooking() + BookingService.java. */
@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final PassengerRepository passengerRepository;
    private final AircraftRepository aircraftRepository;
    private final RouteRepository routeRepository;
    private final CostCalculatorService costCalculatorService;
    private final BookingRulesService bookingRulesService;
    private final PaymentRepository paymentRepository;

    public BookingService(BookingRepository bookingRepository, PassengerRepository passengerRepository,
                           AircraftRepository aircraftRepository, RouteRepository routeRepository,
                           CostCalculatorService costCalculatorService, BookingRulesService bookingRulesService,
                           PaymentRepository paymentRepository) {
        this.bookingRepository = bookingRepository;
        this.passengerRepository = passengerRepository;
        this.aircraftRepository = aircraftRepository;
        this.routeRepository = routeRepository;
        this.costCalculatorService = costCalculatorService;
        this.bookingRulesService = bookingRulesService;
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public Booking createBooking(String userEmail, CreateBookingRequest req) {
        if (req.getType() == null || (!req.getType().equals("Domestic Charter") && !req.getType().equals("Helicopter Charter"))) {
            throw new BadRequestException("Charter type must be 'Domestic Charter' or 'Helicopter Charter'.");
        }
        if (req.getTripType() == null || (!req.getTripType().equals("One Way") && !req.getTripType().equals("Round Trip"))) {
            throw new BadRequestException("Trip type must be 'One Way' or 'Round Trip'.");
        }
        if (routeRepository.findById(req.getOrigin()).isEmpty() || routeRepository.findById(req.getDestination()).isEmpty()) {
            throw new BadRequestException("Unknown origin or destination airport code.");
        }
        if (req.getDate() == null || req.getDate().compareTo(IdGen.todayIso()) < 0) {
            throw new BadRequestException("Departure date cannot be in the past.");
        }
        boolean roundTrip = "Round Trip".equals(req.getTripType());
        if (roundTrip) {
            if (req.getReturnDate() == null || req.getReturnDate().compareTo(req.getDate()) < 0) {
                throw new BadRequestException("Return date cannot be before the departure date.");
            }
        }
        if (req.getPax() < 1 || req.getPax() > 14) {
            throw new BadRequestException("Number of passengers must be between 1 and 14.");
        }
        if (req.getPassengers() == null || req.getPassengers().size() != req.getPax()) {
            throw new BadRequestException("Passenger details must be provided for every passenger.");
        }

        Aircraft aircraft = aircraftRepository.findById(req.getAircraftId())
                .orElseThrow(() -> new NotFoundException("Selected aircraft not found."));
        if (!"Available".equals(aircraft.getStatus())) {
            throw new BadRequestException("Selected aircraft is no longer available.");
        }
        if (aircraft.getCapacity() < req.getPax()) {
            throw new BadRequestException("Selected aircraft does not have enough seats.");
        }
        String neededCategory = "Helicopter Charter".equals(req.getType()) ? "Helicopter" : null;
        if (neededCategory != null && !neededCategory.equals(aircraft.getCategory())) {
            throw new BadRequestException("Selected aircraft is not a helicopter.");
        }

        boolean isSelfFly = req.isSelfFly();
        if (isSelfFly) {
            if (req.getSelfFlyDetails() == null) {
                throw new BadRequestException("Self-fly details are required.");
            }
            var sf = req.getSelfFlyDetails();
            String licErr = Validators.licenseNumber(sf.getLicenseNumber());
            if (!licErr.isEmpty()) throw new BadRequestException(licErr);
            if (!sf.isVerified() && sf.getFlyingHours() < 100) {
                throw new BadRequestException("Self-fly requires at least 100 logged flying hours.");
            }
            if (sf.isVerified() && sf.getFlyingHours() < 100) {
                throw new BadRequestException("DGCA record shows fewer than 100 flying hours - self-fly not permitted.");
            }
            if (!sf.isDgcaDeclaration()) {
                throw new BadRequestException("DGCA self-fly declaration must be accepted.");
            }
        }

        for (PassengerRequest p : req.getPassengers()) {
            String nameErr = Validators.name(p.getName());
            if (!nameErr.isEmpty()) throw new BadRequestException(nameErr);
            String dobErr = Validators.dob(p.getDob());
            if (!dobErr.isEmpty()) throw new BadRequestException(dobErr);
        }

        int distanceKm = costCalculatorService.routeDistanceKm(req.getOrigin(), req.getDestination());
        CostBreakdown cost = costCalculatorService.calculateCost(
                aircraft.getHourlyRate(), req.getType(), req.getTripType(), isSelfFly, distanceKm, aircraft.getSpeed());

        Booking b = new Booking();
        b.setId(IdGen.uid("BKG"));
        b.setUserEmail(userEmail);
        b.setType(req.getType());
        b.setTripType(req.getTripType());
        b.setOrigin(req.getOrigin());
        b.setDestination(req.getDestination());
        b.setDate(req.getDate());
        b.setTime(req.getTime());
        b.setReturnDate(req.getReturnDate() == null ? "" : req.getReturnDate());
        b.setReturnTime(req.getReturnTime() == null ? "" : req.getReturnTime());
        b.setPax(req.getPax());
        b.setAircraftId(aircraft.getId());
        b.setAircraftModel(aircraft.getModel());
        b.setSelfFly(isSelfFly);
        if (isSelfFly) {
            var sf = req.getSelfFlyDetails();
            b.setLicenseNumber(sf.getLicenseNumber());
            b.setLicenseClass(sf.getLicenseClass());
            b.setFlyingHours(sf.getFlyingHours());
            b.setCertificateFileName(sf.getCertificateFileName());
            b.setDgcaDeclaration(sf.isDgcaDeclaration());
            b.setLicenseVerified(sf.isVerified());
        }
        b.setHours(cost.hours);
        b.setAircraftCost(cost.aircraftCost);
        b.setPilotCost(cost.pilotCost);
        b.setCrewCost(cost.crewCost);
        b.setAirportCharges(cost.airportCharges);
        b.setFuelSurcharge(cost.fuelSurcharge);
        b.setGst(cost.gst);
        b.setTotal(cost.total);
        b.setStatus("Pending Payment");
        b.setCreatedAt(IdGen.nowIso());
        bookingRepository.save(b);

        for (PassengerRequest pr : req.getPassengers()) {
            Passenger p = new Passenger();
            p.setBookingId(b.getId());
            p.setName(pr.getName());
            p.setDob(pr.getDob());
            p.setGender(pr.getGender());
            p.setAadhaar(pr.getAadhaar());
            p.setVerificationStatus(pr.getVerificationStatus());
            p.setNoAadhaar(pr.isNoAadhaar());
            p.setAltDocumentId(pr.getAltDocumentId());
            passengerRepository.save(p);
        }

        aircraft.setStatus("Booked");
        aircraftRepository.save(aircraft);

        bookingRulesService.addAudit(userEmail, "Booking", "Booking Created", b.getId());
        bookingRulesService.addNotification(userEmail, "Booking Created",
                "Booking " + b.getId() + " has been created. Complete payment to proceed.", "success");
        bookingRulesService.addNotification("admin", "New Booking",
                "New booking " + b.getId() + " created by " + userEmail, "info");

        return b;
    }

    public List<Booking> findByUser(String userEmail) {
        return bookingRepository.findByUserEmailOrderByCreatedAtDesc(userEmail);
    }

    public List<Booking> findAll() {
        return bookingRepository.findAllByOrderByCreatedAtDesc();
    }

    public Booking findById(String id) {
        return bookingRepository.findById(id).orElseThrow(() -> new NotFoundException("Booking not found."));
    }

    public Booking findOwnedByUser(String id, String userEmail) {
        Booking b = findById(id);
        if (!b.getUserEmail().equals(userEmail)) throw new ForbiddenException("This booking does not belong to you.");
        return b;
    }

    public List<Passenger> findPassengers(String bookingId) {
        return passengerRepository.findByBookingId(bookingId);
    }

    @Transactional
    public void cancelBooking(String userEmail, String bookingId) {
        Booking b = findOwnedByUser(bookingId, userEmail);
        if (!bookingRulesService.isCancellable(b.getStatus())) {
            throw new BadRequestException("Bookings with status \"" + b.getStatus() + "\" cannot be self-cancelled.");
        }

        var paymentOpt = paymentRepository.findByBookingId(bookingId);
        long base = paymentOpt.filter(p -> "VERIFIED".equals(p.getStatus())).map(p -> p.getAmount()).orElse(b.getTotal());
        long fee = Math.round(base * 0.20);
        long refund = base - fee;

        paymentOpt.filter(p -> "VERIFIED".equals(p.getStatus())).ifPresent(p -> {
            p.setCancellationFee(fee);
            p.setRefundAmount(refund);
            p.setStatus("RETURNED");
            paymentRepository.save(p);
        });

        bookingRulesService.releaseBookingResources(bookingId);
        bookingRulesService.voidUnsignedLease(bookingId);

        b.setStatus("Cancelled");
        b.setAssignedPilotId(null);
        b.setAssignedCrewIds(null);
        bookingRepository.save(b);

        bookingRulesService.addAudit(userEmail, "Booking", "Booking Cancelled", bookingId + " fee=" + fee + " refund=" + refund);
        bookingRulesService.addNotification(userEmail, "Booking Cancelled",
                "Booking " + bookingId + " was cancelled. Refund of INR " + refund + " will be processed.", "info");
        bookingRulesService.addNotification("admin", "Booking Cancelled", "Customer cancelled booking " + bookingId, "info");
    }
}
