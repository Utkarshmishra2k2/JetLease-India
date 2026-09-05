package com.jetlease.model.service;

import java.sql.SQLException;
import java.util.List;

import com.jetlease.model.dao.AircraftDao;
import com.jetlease.model.dao.BookingDao;
import com.jetlease.model.dao.PassengerDao;
import com.jetlease.model.entity.Aircraft;
import com.jetlease.model.entity.Booking;
import com.jetlease.model.entity.Passenger;

public class BookingService {

    public static class PassengerDraft {
        public String name, dob, gender, aadhaar, verificationStatus = "Not Checked";
        public boolean noAadhaar;
        public String altDocumentId;
    }

    public static class SelfFlyDetails {
        public String licenseNumber, licenseClass, certificateFileName;
        public int flyingHours;
        public boolean dgcaDeclaration, verified;
    }

    public static Booking createBooking(String userEmail, String bookingType, String tripType,
                                        String origin, String destination, String date, String time,
                                        String returnDate, String returnTime, int pax, Aircraft aircraft,
                                        SelfFlyDetails selfFly, List<PassengerDraft> passengers,
                                        CostCalculator.Cost cost) throws SQLException {
        String bookingId = IdGen.uid("BKG");
        boolean isSelfFly = selfFly != null;

        Booking b = new Booking();
        b.setId(bookingId);
        b.setUserEmail(userEmail);
        b.setType(bookingType);
        b.setTripType(tripType);
        b.setOrigin(origin);
        b.setDestination(destination);
        b.setDate(date);
        b.setTime(time);
        b.setReturnDate(returnDate);
        b.setReturnTime(returnTime);
        b.setPax(pax);
        b.setAircraftId(aircraft.getId());
        b.setAircraftModel(aircraft.getModel());
        b.setSelfFly(isSelfFly);
        b.setLicenseNumber(isSelfFly ? selfFly.licenseNumber : null);
        b.setLicenseClass(isSelfFly ? selfFly.licenseClass : null);
        b.setFlyingHours(isSelfFly ? selfFly.flyingHours : 0);
        b.setCertificateFileName(isSelfFly ? selfFly.certificateFileName : null);
        b.setDgcaDeclaration(isSelfFly && selfFly.dgcaDeclaration);
        b.setLicenseVerified(isSelfFly && selfFly.verified);
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

        BookingDao.save(b);

        for (PassengerDraft pd : passengers) {
            Passenger p = new Passenger();
            p.setBookingId(bookingId);
            p.setName(pd.name);
            p.setDob(pd.dob);
            p.setGender(pd.gender);
            p.setAadhaar(pd.aadhaar);
            p.setVerificationStatus(pd.verificationStatus);
            p.setNoAadhaar(pd.noAadhaar);
            p.setAltDocumentId(pd.altDocumentId);
            PassengerDao.save(p);
        }

        AircraftDao.updateStatus(aircraft.getId(), "Booked");

        BookingRules.addAudit(userEmail, "Booking", "Booking Created", bookingId + " - " + aircraft.getModel());
        BookingRules.addNotification(userEmail, "Booking Created",
                "Your booking " + bookingId + " has been created. Total due: " + com.jetlease.view.ConsoleUtil.fmtInr(cost.total), "success");
        BookingRules.addNotification("admin", "New Booking",
                "New booking " + bookingId + " from " + userEmail, "info");

        return b;
    }
}

