package com.jetlease.controller;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.jetlease.view.ConsoleUtil.*;

import com.jetlease.model.dao.AircraftDao;
import com.jetlease.model.dao.AuditLogDao;
import com.jetlease.model.dao.BookingDao;
import com.jetlease.model.dao.ContactMessageDao;
import com.jetlease.model.dao.CrewDao;
import com.jetlease.model.dao.Db;
import com.jetlease.model.dao.LeaseDao;
import com.jetlease.model.dao.PaymentDao;
import com.jetlease.model.dao.PilotDao;
import com.jetlease.model.dao.ReportDao;
import com.jetlease.model.dao.RouteDao;
import com.jetlease.model.dao.UserDao;
import com.jetlease.model.entity.Aircraft;
import com.jetlease.model.entity.AuditLog;
import com.jetlease.model.entity.Booking;
import com.jetlease.model.entity.ContactMessage;
import com.jetlease.model.entity.Crew;
import com.jetlease.model.entity.Lease;
import com.jetlease.model.entity.Payment;
import com.jetlease.model.entity.Pilot;
import com.jetlease.model.entity.Report;
import com.jetlease.model.entity.Route;
import com.jetlease.model.entity.User;
import com.jetlease.model.service.AuthService;
import com.jetlease.model.service.BookingRules;
import com.jetlease.model.service.IdGen;
import com.jetlease.model.service.MockApi;

public class AdminController {

    public static void run(String adminEmail) throws SQLException {
        while (true) {
        	com.jetlease.view.AdminView.displayAdminMenu();
            int choice = readIntInRange("Choose: ", 0, 11);
            switch (choice) {
                case 1: overview(); break;
                case 2: aircraftMenu(adminEmail); break;
                case 3: bookingsMenu(adminEmail); break;
                case 4: paymentsMenu(adminEmail); break;
                case 5: leasesMenu(adminEmail); break;
                case 6: customersMenu(adminEmail); break;
                case 7: crewPilotsMenu(adminEmail); break;
                case 8: routesMenu(); break;
                case 9: inboxMenu(adminEmail); break;
                case 10: exportsMenu(); break;
                case 11: auditLogMenu(); break;
                case 0: return;
            }
        }
    }

    private static void overview() throws SQLException {
    	com.jetlease.view.AdminView.displayOverviewHeader();
        ResultSet rs = Db.getConnection().createStatement().executeQuery("SELECT COUNT(*) c FROM bookings");
        rs.next(); printLine("Total Bookings", rs.getInt("c")); rs.close();

        rs = Db.getConnection().createStatement().executeQuery(
                "SELECT status, COUNT(*) c FROM bookings GROUP BY status");
        System.out.println("\nBookings by status:");
        while (rs.next()) System.out.println("  " + rs.getString("status") + ": " + rs.getInt("c"));
        rs.close();

        rs = Db.getConnection().createStatement().executeQuery(
                "SELECT SUM(total) s FROM bookings WHERE status = 'Completed'");
        rs.next();
        printLine("\nRevenue (Completed bookings)", fmtInr(rs.getLong("s")));
        rs.close();

        rs = Db.getConnection().createStatement().executeQuery("SELECT status, COUNT(*) c FROM aircraft GROUP BY status");
        System.out.println("\nAircraft by status:");
        while (rs.next()) System.out.println("  " + rs.getString("status") + ": " + rs.getInt("c"));
        rs.close();

        rs = Db.getConnection().createStatement().executeQuery(
                "SELECT aircraft_model, COUNT(*) c FROM bookings GROUP BY aircraft_model ORDER BY c DESC LIMIT 3");
        System.out.println("\nMost popular aircraft:");
        while (rs.next()) System.out.println("  " + rs.getString("aircraft_model") + ": " + rs.getInt("c") + " bookings");
        rs.close();
        pause();
    }

    private static void aircraftMenu(String adminEmail) throws SQLException {
        while (true) {
            printHeader("Aircraft");
            List<Aircraft> fleet = AircraftDao.findAll();
            List<String> ids = new ArrayList<>();
            int n = 1;
            for (Aircraft a : fleet) {
                ids.add(a.getId());
                System.out.println(n + ") " + a.getModel() + " (" + a.getReg() + ") - "
                        + a.getCategory() + " - " + a.getStatus() + " - " + fmtInr(a.getHourlyRate()) + "/hr");
                n++;
            }
            System.out.println("\na) Add new aircraft   0) Back");
            String input = readLine("Choose a number, 'a', or 0: ");
            if (input.equals("0")) return;
            if (input.equalsIgnoreCase("a")) { addAircraft(adminEmail); continue; }
            int idx;
            try { idx = Integer.parseInt(input); } catch (NumberFormatException e) { continue; }
            if (idx < 1 || idx > ids.size()) continue;
            editAircraft(adminEmail, ids.get(idx - 1));
        }
    }

    private static void addAircraft(String adminEmail) throws SQLException {
        printHeader("Add Aircraft");
        String reg = readLine("Registration: ");
        String model = readValidated("Model: ", v -> v.trim().isEmpty() ? "Model is required." : "");
        String manufacturer = readLine("Manufacturer: ");
        System.out.println("Category: 1) Light Jet 2) Mid Jet 3) Heavy Jet 4) Helicopter 5) Turboprop 6) Air Ambulance");
        String[] cats = {"Light Jet", "Mid Jet", "Heavy Jet", "Helicopter", "Turboprop", "Air Ambulance"};
        String category = cats[readIntInRange("Choose: ", 1, 6) - 1];
        int capacity = readInt("Capacity: ");
        int speed = readInt("Speed (km/h): ");
        int range = readInt("Range (km): ");
        long rate = (long) readDouble("Hourly Rate (INR): ");
        String typeRating = readLine("Type Rating code: ");

        Aircraft a = new Aircraft(IdGen.uid("AC"), reg, model, manufacturer, category, capacity, speed, range, rate, "Available", typeRating);
        AircraftDao.save(a);

        BookingRules.addAudit(adminEmail, "Admin", "Aircraft Added", a.getId() + " - " + model);
        System.out.println("Aircraft added.");
    }

    private static void editAircraft(String adminEmail, String id) throws SQLException {
        Aircraft a = AircraftDao.findById(id);
        if (a == null) return;
        String model = a.getModel();
        String status = a.getStatus();

        printHeader(model + " - " + status);
        System.out.println("1) Change Status   2) Change Hourly Rate   3) Delete Aircraft   0) Back");
        int choice = readIntInRange("Choose: ", 0, 3);
        if (choice == 0) return;

        if (choice == 1) {
            System.out.println("1) Available 2) Booked 3) Maintenance 4) Grounded 5) Retired");
            String[] statuses = {"Available", "Booked", "Maintenance", "Grounded", "Retired"};
            String newStatus = statuses[readIntInRange("Choose: ", 1, 5) - 1];
            AircraftDao.updateStatus(id, newStatus);
            BookingRules.addAudit(adminEmail, "Admin", "Aircraft Status Changed", id + " -> " + newStatus);
            System.out.println("Status updated.");
        } else if (choice == 2) {
            long newRate = (long) readDouble("New Hourly Rate (INR): ");
            AircraftDao.updateHourlyRate(id, newRate);
            BookingRules.addAudit(adminEmail, "Admin", "Aircraft Rate Changed", id + " -> " + fmtInr(newRate));
            System.out.println("Rate updated.");
        } else if (choice == 3) {
            if (status.equals("Booked")) {
                System.out.println("  ! Cannot delete an aircraft that is currently booked.");
                return;
            }
            if (!readYesNo("Really delete this aircraft?")) return;
            AircraftDao.delete(id);
            BookingRules.addAudit(adminEmail, "Admin", "Aircraft Deleted", id + " - " + model);
            System.out.println("Aircraft deleted.");
        }
    }

    private static void bookingsMenu(String adminEmail) throws SQLException {
        while (true) {
            printHeader("Bookings");
            List<Booking> bookings = BookingDao.findAll();
            List<String> ids = new ArrayList<>();
            int n = 1;
            for (Booking b : bookings) {
                ids.add(b.getId());
                System.out.println(n + ") " + b.getId() + " - " + b.getUserEmail() + " - "
                        + b.getAircraftModel() + " - " + b.getStatus());
                n++;
            }
            if (ids.isEmpty()) { System.out.println("No bookings yet."); pause(); return; }
            int choice = readIntInRange("\nOpen which booking? (0 to go back): ", 0, ids.size());
            if (choice == 0) return;
            openBookingAdmin(adminEmail, ids.get(choice - 1));
        }
    }

    private static void openBookingAdmin(String adminEmail, String bookingId) throws SQLException {
        Booking b = BookingDao.findById(bookingId);
        if (b == null) return;
        String status = b.getStatus();
        boolean selfFly = b.isSelfFly();
        String userEmail = b.getUserEmail();

        printHeader("Booking " + bookingId + " - " + status);
        List<String> actions = new ArrayList<>();
        if (BookingRules.isActive(status) && !selfFly) actions.add("Assign Pilot & Crew");
        if (status.equals("Lease Signed")) actions.add("Approve (advance to Approved)");
        if (status.equals("Approved")) actions.add("Dispatch");
        if (status.equals("Dispatched")) actions.add("Mark Completed");
        if (!BookingRules.ENDED_STATUSES.contains(status)) actions.add("Reject Booking");
        actions.add("Back");

        for (int i = 0; i < actions.size(); i++) System.out.println((i + 1) + ") " + actions.get(i));
        int choice = readIntInRange("Choose: ", 1, actions.size());
        String action = actions.get(choice - 1);

        switch (action) {
            case "Assign Pilot & Crew": assignCrew(adminEmail, bookingId); break;
            case "Approve (advance to Approved)": advanceBooking(adminEmail, bookingId, "Approved", userEmail); break;
            case "Dispatch": advanceBooking(adminEmail, bookingId, "Dispatched", userEmail); break;
            case "Mark Completed": completeBooking(adminEmail, bookingId, userEmail); break;
            case "Reject Booking": rejectBooking(adminEmail, bookingId, userEmail); break;
            default: break;
        }
    }

    private static void assignCrew(String adminEmail, String bookingId) throws SQLException {
        Booking b = BookingDao.findById(bookingId);
        if (b == null) return;
        double hours = b.getHours();

        printHeader("Assign Pilot");
        List<Pilot> pilots = PilotDao.findAll();
        List<String> pilotIds = new ArrayList<>();
        int n = 1;
        for (Pilot p : pilots) {
            if (p.isAvailable()) {
                pilotIds.add(p.getId());
                System.out.println(n + ") " + p.getName() + " - remaining hours: " + p.getRemainingHours());
                n++;
            }
        }
        if (pilotIds.isEmpty()) { System.out.println("No pilots currently available."); return; }
        int pChoice = readIntInRange("Choose pilot (0 to cancel): ", 0, pilotIds.size());
        if (pChoice == 0) return;
        String pilotId = pilotIds.get(pChoice - 1);

        Pilot chosenPilot = PilotDao.findById(pilotId);
        if (chosenPilot == null || chosenPilot.getRemainingHours() < hours) {
            System.out.println("  ! This pilot does not have enough remaining hours for this flight.");
            return;
        }

        printHeader("Assign Crew (choose 1 or more)");
        List<Crew> crewList = CrewDao.findAll();
        List<String> crewIds = new ArrayList<>();
        n = 1;
        for (Crew c : crewList) {
            if (c.isAvailable()) {
                crewIds.add(c.getId());
                System.out.println(n + ") " + c.getName() + " (" + c.getRole() + ") - remaining hours: " + c.getRemainingHours());
                n++;
            }
        }
        if (crewIds.isEmpty()) { System.out.println("No crew currently available."); return; }
        String pick = readLine("Enter crew numbers separated by commas (e.g. 1,3): ");
        List<String> chosenCrew = new ArrayList<>();
        for (String part : pick.split(",")) {
            part = part.trim();
            if (part.isEmpty()) continue;
            int idx;
            try { idx = Integer.parseInt(part); } catch (NumberFormatException e) { continue; }
            if (idx >= 1 && idx <= crewIds.size()) chosenCrew.add(crewIds.get(idx - 1));
        }
        if (chosenCrew.isEmpty()) { System.out.println("No valid crew selected. Cancelled."); return; }

        for (String cid : chosenCrew) {
            Crew c = CrewDao.findById(cid);
            if (c == null || c.getRemainingHours() < hours) {
                System.out.println("  ! Crew member " + cid + " does not have enough remaining hours. Assignment cancelled.");
                return;
            }
        }

        PreparedStatement updP = Db.getConnection().prepareStatement("UPDATE pilots SET remaining_hours = remaining_hours - ? WHERE id = ?");
        updP.setDouble(1, hours);
        updP.setString(2, pilotId);
        updP.executeUpdate();
        updP.close();

        StringBuilder crewCsv = new StringBuilder();
        for (String cid : chosenCrew) {
            PreparedStatement updC = Db.getConnection().prepareStatement("UPDATE crew SET remaining_hours = remaining_hours - ? WHERE id = ?");
            updC.setDouble(1, hours);
            updC.setString(2, cid);
            updC.executeUpdate();
            updC.close();
            if (crewCsv.length() > 0) crewCsv.append(",");
            crewCsv.append(cid);
        }

        BookingDao.updateAssignedPilot(bookingId, pilotId);
        BookingDao.updateAssignedCrew(bookingId, crewCsv.toString());

        BookingRules.addAudit(adminEmail, "Admin", "Crew Assigned", bookingId + " pilot=" + pilotId + " crew=" + crewCsv);
        System.out.println("Pilot and crew assigned.");
    }

    private static void advanceBooking(String adminEmail, String bookingId, String newStatus, String userEmail) throws SQLException {
        BookingDao.updateStatus(bookingId, newStatus);
        BookingRules.addAudit(adminEmail, "Admin", "Booking Advanced", bookingId + " -> " + newStatus);
        BookingRules.addNotification(userEmail, "Booking Update", "Your booking " + bookingId + " is now \"" + newStatus + "\".", "info");
        System.out.println("Booking updated to " + newStatus + ".");
    }

    private static void completeBooking(String adminEmail, String bookingId, String userEmail) throws SQLException {
        Booking b = BookingDao.findById(bookingId);
        if (b == null) return;
        long total = b.getTotal();

        advanceBooking(adminEmail, bookingId, "Completed", userEmail);

        int points = (int) Math.round(total / 10000.0);
        UserDao.updateLoyaltyPoints(userEmail, points);

        BookingRules.addNotification(userEmail, "Loyalty Points Earned", "You earned " + points + " loyalty points for booking " + bookingId + ".", "success");
    }

    private static void rejectBooking(String adminEmail, String bookingId, String userEmail) throws SQLException {
        if (!readYesNo("Really reject this booking?")) return;

        Payment p = PaymentDao.findByBookingId(bookingId);
        if (p != null && "VERIFIED".equals(p.getStatus())) {
            PaymentDao.updateRefund(p.getId(), 0, p.getAmount(), "RETURNED");
        }

        BookingRules.releaseBookingResources(bookingId);
        BookingRules.voidUnsignedLease(bookingId);

        BookingDao.updateStatus(bookingId, "Rejected");
        BookingDao.updateAssignedPilot(bookingId, null);
        BookingDao.updateAssignedCrew(bookingId, null);

        BookingRules.addAudit(adminEmail, "Admin", "Booking Rejected", bookingId);
        BookingRules.addNotification(userEmail, "Booking Rejected", "Your booking " + bookingId + " was rejected. Any verified payment will be fully refunded.", "warning");
        System.out.println("Booking rejected.");
    }

    private static void paymentsMenu(String adminEmail) throws SQLException {
        while (true) {
            printHeader("Payments");
            List<Payment> payments = PaymentDao.findAll();
            List<String> ids = new ArrayList<>();
            int n = 1;
            for (Payment p : payments) {
                ids.add(p.getId());
                System.out.println(n + ") " + p.getId() + " - " + p.getBookingId() + " - "
                        + p.getUserEmail() + " - " + fmtInr(p.getAmount()) + " - " + p.getStatus());
                n++;
            }
            if (ids.isEmpty()) { System.out.println("No payments yet."); pause(); return; }
            int choice = readIntInRange("\nOpen which payment? (0 to go back): ", 0, ids.size());
            if (choice == 0) return;
            openPaymentAdmin(adminEmail, ids.get(choice - 1));
        }
    }

    private static void openPaymentAdmin(String adminEmail, String paymentId) throws SQLException {
        Payment p = PaymentDao.findById(paymentId);
        if (p == null) return;
        String status = p.getStatus();
        String bookingId = p.getBookingId();
        String userEmail = p.getUserEmail();
        long amount = p.getAmount();
        String txnId = p.getTransactionId();

        if (!status.equals("PENDING_VERIFICATION")) {
            System.out.println("This payment is already \"" + status + "\" - nothing to verify.");
            return;
        }

        MockApi.VerifyResult result = MockApi.verifyPaymentAgainstLedger(txnId, bookingId, amount);
        System.out.println("  " + result.message);

        boolean proceed = result.verified;
        if (!result.verified) proceed = readYesNo("Ledger check failed. Verify anyway?");
        if (!proceed) {
            System.out.println("No action taken.");
            return;
        }

        System.out.println("1) Verify Payment   2) Reject Payment   0) Cancel");
        int choice = readIntInRange("Choose: ", 0, 2);
        if (choice == 0) return;

        if (choice == 1) {
            PaymentDao.updateStatus(paymentId, "VERIFIED");
            BookingDao.updateStatus(bookingId, "Lease Pending");

            BookingRules.ensureLeaseForBooking(bookingId, userEmail);
            BookingRules.addAudit(adminEmail, "Admin", "Payment Verified", paymentId);
            BookingRules.addNotification(userEmail, "Payment Verified", "Your payment for " + bookingId + " has been verified. Your lease is ready.", "success");
            System.out.println("Payment verified.");
        } else {
            PaymentDao.updateStatus(paymentId, "REJECTED");
            BookingDao.updateStatus(bookingId, "Payment Rejected");

            BookingRules.addAudit(adminEmail, "Admin", "Payment Rejected", paymentId);
            BookingRules.addNotification(userEmail, "Payment Rejected", "Your payment for " + bookingId + " was rejected. Please resubmit.", "warning");
            System.out.println("Payment rejected.");
        }
    }

    private static void leasesMenu(String adminEmail) throws SQLException {
        while (true) {
            printHeader("Leases");
            List<Lease> leases = LeaseDao.findAll();
            List<String> ids = new ArrayList<>();
            int n = 1;
            for (Lease l : leases) {
                ids.add(l.getId());
                System.out.println(n + ") " + l.getId() + " - " + l.getBookingId() + " - "
                        + l.getUserEmail() + " - " + l.getStatus());
                n++;
            }
            if (ids.isEmpty()) { System.out.println("No leases yet."); pause(); return; }
            int choice = readIntInRange("\nOpen which lease? (0 to go back): ", 0, ids.size());
            if (choice == 0) return;
            openLeaseAdmin(adminEmail, ids.get(choice - 1));
        }
    }

    private static void openLeaseAdmin(String adminEmail, String leaseId) throws SQLException {
        Lease l = LeaseDao.findById(leaseId);
        if (l == null) return;
        String status = l.getStatus();
        String bookingId = l.getBookingId();
        String userEmail = l.getUserEmail();

        if (!status.equals("Signed")) {
            System.out.println("Only a lease with status \"Signed\" can be approved/rejected here. Current status: " + status);
            return;
        }

        System.out.println("1) Approve Lease   2) Reject Lease   0) Cancel");
        int choice = readIntInRange("Choose: ", 0, 2);
        if (choice == 0) return;

        if (choice == 1) {
            LeaseDao.updateStatus(leaseId, "Approved", IdGen.todayIso());
            BookingDao.updateStatus(bookingId, "Approved");

            BookingRules.addAudit(adminEmail, "Admin", "Lease Approved", leaseId);
            BookingRules.addNotification(userEmail, "Lease Approved", "Your lease for booking " + bookingId + " has been approved.", "success");
            System.out.println("Lease approved.");
        } else {
            LeaseDao.updateStatus(leaseId, "Rejected", null);

            Payment p = PaymentDao.findByBookingId(bookingId);
            if (p != null && "VERIFIED".equals(p.getStatus())) {
                PaymentDao.updateRefund(p.getId(), 0, p.getAmount(), "RETURNED");
            }

            BookingRules.releaseBookingResources(bookingId);

            BookingDao.updateStatus(bookingId, "Rejected");
            BookingDao.updateAssignedPilot(bookingId, null);
            BookingDao.updateAssignedCrew(bookingId, null);

            BookingRules.addAudit(adminEmail, "Admin", "Lease Rejected", leaseId);
            BookingRules.addNotification(userEmail, "Lease Rejected", "Your lease for booking " + bookingId + " was rejected and your payment is being fully refunded.", "warning");
            System.out.println("Lease rejected.");
        }
    }

    private static void customersMenu(String adminEmail) throws SQLException {
        while (true) {
            printHeader("Customers");
            List<User> customers = UserDao.findAllCustomers();
            List<String> emails = new ArrayList<>();
            int n = 1;
            for (User u : customers) {
                emails.add(u.getEmail());
                System.out.println(n + ") " + u.getFullName() + " <" + u.getEmail() + "> - " + u.getStatus());
                n++;
            }
            if (emails.isEmpty()) { System.out.println("No customers yet."); pause(); return; }
            int choice = readIntInRange("\nOpen which customer? (0 to go back): ", 0, emails.size());
            if (choice == 0) return;
            openCustomerAdmin(adminEmail, emails.get(choice - 1));
        }
    }

    private static void openCustomerAdmin(String adminEmail, String email) throws SQLException {
        String status = AuthService.getUserField(email, "status");
        printHeader(email + " - " + status);

        System.out.println("Booking history:");
        List<Booking> bookings = BookingDao.findByUserEmail(email);
        for (Booking b : bookings) {
            System.out.println("  " + b.getId() + " - " + b.getStatus() + " - " + fmtInr(b.getTotal()));
        }

        System.out.println("\n1) " + (status.equals("suspended") ? "Reactivate" : "Suspend") + " Account   0) Back");
        int choice = readIntInRange("Choose: ", 0, 1);
        if (choice == 0) return;

        String newStatus = status.equals("suspended") ? "active" : "suspended";
        UserDao.updateUserField(email, "status", newStatus);

        BookingRules.addAudit(adminEmail, "Admin", "Customer " + (newStatus.equals("suspended") ? "Suspended" : "Reactivated"), email);
        BookingRules.addNotification(email, "Account " + (newStatus.equals("suspended") ? "Suspended" : "Reactivated"),
                "Your account has been " + newStatus + " by the JetLease team.", "warning");
        System.out.println("Customer account " + newStatus + ".");
    }

    private static void crewPilotsMenu(String adminEmail) throws SQLException {
        printHeader("Pilots");
        List<Pilot> pilots = PilotDao.findAll();
        List<String> pilotIds = new ArrayList<>();
        int n = 1;
        for (Pilot p : pilots) {
            pilotIds.add(p.getId());
            System.out.println(n + ") " + p.getName() + " - hours remaining: " + p.getRemainingHours()
                    + " - " + (p.isAvailable() ? "Available" : "Unavailable"));
            n++;
        }

        System.out.println("\nCrew:");
        List<Crew> crewList = CrewDao.findAll();
        List<String> crewIds = new ArrayList<>();
        n = 1;
        for (Crew c : crewList) {
            crewIds.add(c.getId());
            System.out.println(n + ") " + c.getName() + " (" + c.getRole() + ") - hours remaining: " + c.getRemainingHours()
                    + " - " + (c.isAvailable() ? "Available" : "Unavailable"));
            n++;
        }

        System.out.println("\n1) Toggle a pilot's availability   2) Toggle a crew member's availability   0) Back");
        int choice = readIntInRange("Choose: ", 0, 2);
        if (choice == 0) return;
        if (choice == 1 && !pilotIds.isEmpty()) {
            int idx = readIntInRange("Which pilot number? ", 1, pilotIds.size());
            Pilot p = PilotDao.findById(pilotIds.get(idx - 1));
            if (p != null) {
                PilotDao.updateAvailability(p.getId(), !p.isAvailable());
                BookingRules.addAudit(adminEmail, "Admin", "Pilot Availability Toggled", p.getId());
            }
        } else if (choice == 2 && !crewIds.isEmpty()) {
            int idx = readIntInRange("Which crew number? ", 1, crewIds.size());
            Crew c = CrewDao.findById(crewIds.get(idx - 1));
            if (c != null) {
                CrewDao.updateAvailability(c.getId(), !c.isAvailable());
                BookingRules.addAudit(adminEmail, "Admin", "Crew Availability Toggled", c.getId());
            }
        }
    }

    private static void routesMenu() throws SQLException {
        printHeader("Routes & Booking Counts");
        List<Route> routes = RouteDao.findAll();
        for (Route r : routes) {
            String code = r.getCode();
            PreparedStatement ps = Db.getConnection().prepareStatement(
                    "SELECT COUNT(*) c FROM bookings WHERE origin = ? OR destination = ?");
            ps.setString(1, code);
            ps.setString(2, code);
            ResultSet rc = ps.executeQuery();
            rc.next();
            System.out.println("  " + code + " - " + r.getCity() + " - " + rc.getInt("c") + " bookings");
            rc.close();
            ps.close();
        }
        pause();
    }

    private static void inboxMenu(String adminEmail) throws SQLException {
        while (true) {
            printHeader("Inbox");
            System.out.println("1) Contact Messages   2) Issue Reports   0) Back");
            int choice = readIntInRange("Choose: ", 0, 2);
            if (choice == 0) return;
            if (choice == 1) contactMessages(adminEmail);
            else issueReports(adminEmail);
        }
    }

    private static void contactMessages(String adminEmail) throws SQLException {
        printHeader("Contact Messages");
        List<ContactMessage> messages = ContactMessageDao.findAll();
        List<String> ids = new ArrayList<>();
        int n = 1;
        for (ContactMessage msg : messages) {
            ids.add(msg.getId());
            System.out.println("\n" + n + ") [" + msg.getStatus() + "] " + msg.getName() + " <" + msg.getEmail() + ">");
            System.out.println("   " + msg.getMessage());
            n++;
        }
        if (ids.isEmpty()) { System.out.println("No messages."); pause(); return; }
        int choice = readIntInRange("\nMark which as Read? (0 to skip): ", 0, ids.size());
        if (choice == 0) return;
        ContactMessageDao.updateStatus(ids.get(choice - 1), "Read");
        BookingRules.addAudit(adminEmail, "Admin", "Contact Message Read", ids.get(choice - 1));
    }

    private static void issueReports(String adminEmail) throws SQLException {
        printHeader("Issue Reports");
        List<Report> reports = ReportDao.findAll();
        List<String> ids = new ArrayList<>();
        List<String> emails = new ArrayList<>();
        List<String> bookingIds = new ArrayList<>();
        int n = 1;
        for (Report r : reports) {
            ids.add(r.getId());
            emails.add(r.getUserEmail());
            bookingIds.add(r.getBookingId());
            System.out.println("\n" + n + ") [" + r.getStatus() + "] " + r.getSubject() + " - " + r.getUserEmail());
            System.out.println("   Booking: " + r.getBookingId());
            System.out.println("   " + r.getDetails());
            n++;
        }
        if (ids.isEmpty()) { System.out.println("No reports."); pause(); return; }
        int choice = readIntInRange("\nMark which as Resolved? (0 to skip): ", 0, ids.size());
        if (choice == 0) return;
        ReportDao.updateStatus(ids.get(choice - 1), "Resolved");
        BookingRules.addAudit(adminEmail, "Admin", "Issue Report Resolved", ids.get(choice - 1));
        BookingRules.addNotification(emails.get(choice - 1), "Issue Resolved",
                "Your reported issue for booking " + bookingIds.get(choice - 1) + " has been resolved.", "success");
    }

    private static void exportsMenu() throws SQLException {
        printHeader("Export Reports (CSV)");
        System.out.println("1) Bookings   2) Customers   3) Payments   0) Back");
        int choice = readIntInRange("Choose: ", 0, 3);
        if (choice == 0) return;
        try {
            if (choice == 1) exportCsv("bookings.csv",
                    "SELECT id,user_email,type,trip_type,origin,destination,date,status,total FROM bookings",
                    new String[]{"id", "user_email", "type", "trip_type", "origin", "destination", "date", "status", "total"});
            else if (choice == 2) exportCsv("customers.csv",
                    "SELECT id,full_name,email,phone,status,membership,loyalty_points FROM users WHERE role='customer'",
                    new String[]{"id", "full_name", "email", "phone", "status", "membership", "loyalty_points"});
            else exportCsv("payments.csv",
                    "SELECT id,booking_id,user_email,amount,transaction_id,status FROM payments",
                    new String[]{"id", "booking_id", "user_email", "amount", "transaction_id", "status"});
        } catch (IOException e) {
            System.out.println("  ! Export failed: " + e.getMessage());
        }
    }

    private static void exportCsv(String fileName, String sql, String[] columns) throws SQLException, IOException {
        ResultSet rs = Db.getConnection().createStatement().executeQuery(sql);
        FileWriter fw = new FileWriter(fileName);
        fw.write(String.join(",", columns) + "\n");
        while (rs.next()) {
            StringBuilder row = new StringBuilder();
            for (int i = 0; i < columns.length; i++) {
                if (i > 0) row.append(",");
                String val = rs.getString(columns[i]);
                row.append(val == null ? "" : val.replace(",", ";"));
            }
            fw.write(row + "\n");
        }
        fw.close();
        rs.close();
        System.out.println("Exported to " + fileName + " in the current folder.");
    }

    private static void auditLogMenu() throws SQLException {
        printHeader("Audit Log");
        System.out.println("Filter by category: 1) All 2) Login 3) Booking 4) Payment 5) Lease 6) Admin");
        int choice = readIntInRange("Choose: ", 1, 6);
        List<AuditLog> logs = AuditLogDao.findAll();
        String filterCat = choice == 1 ? null : new String[]{null, "Login", "Booking", "Payment", "Lease", "Admin"}[choice - 1];

        int shown = 0;
        for (AuditLog log : logs) {
            if (filterCat != null && !filterCat.equals(log.getCategory())) continue;
            if (shown >= 100) break;
            System.out.println(log.getTimestamp() + " [" + log.getCategory() + "] "
                    + log.getActor() + " - " + log.getAction() + " - " + log.getDetails());
            shown++;
        }
        pause();
    }
}
