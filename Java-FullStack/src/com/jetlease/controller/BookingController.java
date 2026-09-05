package com.jetlease.controller;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.jetlease.view.ConsoleUtil.*;

import com.jetlease.model.dao.AircraftDao;
import com.jetlease.model.dao.RouteDao;
import com.jetlease.model.entity.Aircraft;
import com.jetlease.model.entity.Booking;
import com.jetlease.model.entity.Route;
import com.jetlease.model.service.BookingService;
import com.jetlease.model.service.CostCalculator;
import com.jetlease.model.service.IdGen;
import com.jetlease.model.service.MockApi;
import com.jetlease.model.service.Validators;
import com.jetlease.view.BookingView;

public class BookingController {

    public static void startNewBooking(String userEmail) throws SQLException {
        printHeader("New Charter Booking - Step 1 of 4: Trip Details");

        System.out.println("Charter Type:  1) Domestic Charter   2) Helicopter Charter");
        String bookingType = readIntInRange("Choose: ", 1, 2) == 1 ? "Domestic Charter" : "Helicopter Charter";

        System.out.println("Trip Type:     1) One Way   2) Round Trip");
        String tripType = readIntInRange("Choose: ", 1, 2) == 1 ? "One Way" : "Round Trip";

        printRoutes();
        String origin = readValidRouteCode("Origin airport code: ");
        String destination = readValidRouteCode("Destination airport code: ");

        String date = readDate("Departure Date");
        if (date.compareTo(IdGen.todayIso()) < 0) {
            System.out.println("  ! Departure date cannot be in the past. Booking cancelled.");
            return;
        }
        String time = readLine("Departure Time (HH:mm): ");

        String returnDate = "", returnTime = "";
        if (tripType.equals("Round Trip")) {
            returnDate = readDate("Return Date");
            if (returnDate.compareTo(date) < 0) {
                System.out.println("  ! Return date cannot be before the departure date. Booking cancelled.");
                return;
            }
            returnTime = readLine("Return Time (HH:mm): ");
        }

        int pax = readIntInRange("Number of Passengers (1-14): ", 1, 14);

        int distanceKm = CostCalculator.routeDistanceKm(origin, destination);
        if (distanceKm > 0 && readYesNo("\nWant an aircraft recommendation based on your budget?")) {
            long budget = (long) readDouble("Your budget (INR): ");
            String category = bookingType.equals("Helicopter Charter") ? "Helicopter" : null;
            List<CostCalculator.Recommendation> scored = CostCalculator.recommendAircraft(pax, budget, distanceKm, category);
            if (scored.isEmpty()) {
                System.out.println("No aircraft currently match your passenger count and range needs.");
            } else {
                CostCalculator.Recommendation best = CostCalculator.bestWithinBudget(scored, budget);
                System.out.println("\nRecommended: " + best.model + " - est. " + fmtInr(best.estCost)
                        + " (capacity " + best.capacity + ", range " + best.range + " km)");
                System.out.println("Other options:");
                int shown = 0;
                for (CostCalculator.Recommendation r : scored) {
                    if (r.id.equals(best.id)) continue;
                    if (shown >= 3) break;
                    System.out.println("  - " + r.model + " - est. " + fmtInr(r.estCost));
                    shown++;
                }
            }
        }

        String neededCategory = bookingType.equals("Helicopter Charter") ? "Helicopter" : null;
        Aircraft aircraft = chooseAircraft(neededCategory, pax);
        if (aircraft == null) {
            System.out.println("No aircraft selected. Booking cancelled.");
            return;
        }

        List<BookingService.PassengerDraft> passengers = collectPassengers(pax);
        if (passengers == null) return;

        BookingService.SelfFlyDetails selfFly = null;
        if (readYesNo("\nIs this a Self-Fly booking (you will pilot the aircraft)?")) {
            selfFly = collectSelfFlyDetails();
            if (selfFly == null) {
                System.out.println("Self-Fly details were invalid. Continuing as a normally crewed charter.");
            }
        }
        boolean isSelfFly = selfFly != null;

        CostCalculator.Cost cost = CostCalculator.calculateCost(aircraft.getHourlyRate(), bookingType, tripType, isSelfFly);
        BookingView.renderCostReview(aircraft, origin, destination, tripType, isSelfFly, cost);

        if (!readYesNo("\nConfirm and create this booking?")) {
            System.out.println("Booking cancelled - nothing was saved.");
            return;
        }

        Booking b = BookingService.createBooking(userEmail, bookingType, tripType, origin, destination,
                date, time, returnDate, returnTime, pax, aircraft, selfFly, passengers, cost);

        System.out.println("\nBooking created! Reference: " + b.getId());
        System.out.println("Go to Payments in your dashboard to complete payment.");
        pause();
    }

    private static void printRoutes() throws SQLException {
        BookingView.displayRoutes(RouteDao.findAll());
    }

    private static String readValidRouteCode(String prompt) throws SQLException {
        while (true) {
            String code = readLine(prompt).toUpperCase();
            Route r = RouteDao.findByCode(code);
            if (r != null) return code;
            System.out.println("  ! Unknown airport code. Please choose one from the list above.");
        }
    }

    private static Aircraft chooseAircraft(String category, int pax) throws SQLException {
        List<Aircraft> fleet = AircraftDao.findAvailable(category, pax);
        if (fleet.isEmpty()) {
            System.out.println("\nAvailable aircraft:\n  (none currently available for this passenger count)");
            return null;
        }
        BookingView.renderAvailableAircraftList(fleet);
        int choice = readIntInRange("Choose aircraft number (0 to cancel): ", 0, fleet.size());
        return choice == 0 ? null : fleet.get(choice - 1);
    }

    private static List<BookingService.PassengerDraft> collectPassengers(int initialCount) throws SQLException {
        List<BookingService.PassengerDraft> passengers = new ArrayList<>();
        printHeader("Step 2 of 4: Passenger Details");
        for (int p = 1; p <= initialCount; p++) {
            System.out.println("\n-- Passenger " + p + " --");
            passengers.add(collectOnePassenger());
        }
        while (true) {
            System.out.println("\nPassengers so far: " + passengers.size());
            System.out.println("1) Add another passenger   2) Remove a passenger   3) Continue");
            int choice = readIntInRange("Choose: ", 1, 3);
            if (choice == 1) {
                if (passengers.size() >= 14) {
                    System.out.println("  ! Maximum of 14 passengers.");
                    continue;
                }
                System.out.println("\n-- Passenger " + (passengers.size() + 1) + " --");
                passengers.add(collectOnePassenger());
            } else if (choice == 2) {
                if (passengers.size() <= 1) {
                    System.out.println("  ! At least one passenger is required.");
                    continue;
                }
                int idx = readIntInRange("Remove which passenger number? ", 1, passengers.size());
                passengers.remove(idx - 1);
            } else {
                break;
            }
        }
        return passengers;
    }

    private static BookingService.PassengerDraft collectOnePassenger() throws SQLException {
        BookingService.PassengerDraft p = new BookingService.PassengerDraft();
        p.name = readValidated("Full Name: ", Validators::name);
        p.dob = readValidated("Date of Birth: ", Validators::dob);
        System.out.println("Gender: 1) Male  2) Female  3) Other");
        int g = readIntInRange("Choose: ", 1, 3);
        p.gender = g == 1 ? "Male" : g == 2 ? "Female" : "Other";

        boolean exempt = Validators.isAadhaarExempt(p.dob);
        boolean hasAadhaar = readYesNo("Does this passenger have an Aadhaar number?");

        if (!hasAadhaar) {
            p.noAadhaar = true;
            p.altDocumentId = readLine("Alternate ID document number/reference: ");
            p.verificationStatus = "Not Applicable";
            return p;
        }

        if (exempt && !readYesNo("Passenger qualifies for the under-5 Aadhaar exemption. Enter Aadhaar anyway?")) {
            p.verificationStatus = "Not Required";
            return p;
        }

        p.aadhaar = readValidated("Aadhaar Number (12 digits): ", Validators::aadhaar);
        MockApi.VerifyResult result = MockApi.verifyAadhaar(p.aadhaar);
        System.out.println("  " + result.message);
        if (result.verified) {
            p.verificationStatus = "Verified";
            p.name = result.holderName;
            p.dob = result.dob;
            p.gender = result.gender;
            System.out.println("  Details auto-filled from Aadhaar record (name, DOB, gender updated).");
        } else {
            p.verificationStatus = "Not Verified";
            System.out.println("  Verification failed, but this does not block your booking.");
        }
        return p;
    }

    private static BookingService.SelfFlyDetails collectSelfFlyDetails() throws SQLException {
        printHeader("Step 3 of 4: Self-Fly Pilot Details");
        BookingService.SelfFlyDetails d = new BookingService.SelfFlyDetails();
        d.licenseNumber = readValidated("Pilot License Number: ", Validators::licenseNumber);

        if (readYesNo("Verify this license with the DGCA registry now?")) {
            MockApi.VerifyResult result = MockApi.verifyPilotLicense(d.licenseNumber);
            System.out.println("  " + result.message);
            if (result.verified) {
                d.verified = true;
                d.flyingHours = result.hoursOnRecord;
                d.licenseClass = result.licenseClass;
                System.out.println("  Flying hours and license class auto-filled and locked from DGCA record.");
            }
        }
        if (!d.verified) {
            System.out.println("License Class: 1) Private Pilot License (PPL)  2) Commercial Pilot License (CPL)  3) Airline Transport Pilot License (ATPL)");
            int c = readIntInRange("Choose: ", 1, 3);
            d.licenseClass = c == 1 ? "Private Pilot License (PPL)" : c == 2 ? "Commercial Pilot License (CPL)" : "Airline Transport Pilot License (ATPL)";
            d.flyingHours = readInt("Total Flying Hours: ");
        }

        d.certificateFileName = readLine("Certificate file name (simulated upload): ");
        d.dgcaDeclaration = readYesNo("Do you declare this information is accurate under DGCA rules?");

        List<String> errors = new ArrayList<>();
        if (d.licenseNumber.length() < 4) errors.add("License number is too short.");
        if (d.licenseClass == null || d.licenseClass.isEmpty()) errors.add("License class is required.");
        if (d.certificateFileName == null || d.certificateFileName.trim().isEmpty()) errors.add("Certificate file is required.");
        if (!d.dgcaDeclaration) errors.add("You must accept the DGCA declaration.");
        if (d.flyingHours < 100) errors.add("A minimum of 100 logged flying hours is required for Self-Fly.");

        if (!errors.isEmpty()) {
            System.out.println("\nSelf-Fly booking rejected:");
            for (String e : errors) System.out.println("  - " + e);
            return null;
        }
        System.out.println("\nSelf-Fly details accepted.");
        return d;
    }
}
