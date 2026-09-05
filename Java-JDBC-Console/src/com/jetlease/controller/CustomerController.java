package com.jetlease.controller;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.jetlease.view.ConsoleUtil.*;

import com.jetlease.model.dao.BookingDao;
import com.jetlease.model.dao.NotificationDao;
import com.jetlease.model.dao.PaymentDao;
import com.jetlease.model.dao.ReportDao;
import com.jetlease.model.dao.UserDao;
import com.jetlease.model.entity.Booking;
import com.jetlease.model.entity.Payment;
import com.jetlease.model.entity.Report;
import com.jetlease.model.entity.User;
import com.jetlease.model.service.BookingRules;
import com.jetlease.model.service.IdGen;
import com.jetlease.model.service.MockApi;
import com.jetlease.model.service.Validators;
import com.jetlease.view.CustomerView;

public class CustomerController {

    public static void run(String email) throws SQLException {
        while (true) {
            User user = UserDao.findByEmail(email);
            String name = user != null ? user.getFullName() : "";
            CustomerView.displayDashboardMenu(name);
            int choice = readIntInRange("Choose: ", 0, 8);
            switch (choice) {
                case 1: overview(email); break;
                case 2: profile(email); break;
                case 3: BookingController.startNewBooking(email); break;
                case 4: myBookings(email); break;
                case 5: PaymentController.listMyPayments(email); break;
                case 6: notifications(email); break;
                case 7: LeaseController.listMyLeases(email); break;
                case 8: reportIssue(email); break;
                case 0: return;
            }
        }
    }

    private static void overview(String email) throws SQLException {
        User user = UserDao.findByEmail(email);
        List<Booking> allBookings = BookingDao.findByUserEmail(email);
        int total = allBookings.size();
        int upcoming = 0;
        List<Booking> upcomingList = new ArrayList<>();
        for (Booking b : allBookings) {
            if (BookingRules.isUpcoming(b.getStatus())) {
                upcoming++;
                if (!b.getStatus().equals("Completed") && !b.getStatus().equals("Cancelled") && !b.getStatus().equals("Rejected")) {
                    upcomingList.add(b);
                }
            }
        }
        CustomerView.renderOverview(user, total, upcoming, upcomingList);
    }

    private static void profile(String email) throws SQLException {
        while (true) {
            User user = UserDao.findByEmail(email);
            CustomerView.renderProfile(user);
            int choice = readIntInRange("Choose: ", 0, 3);
            if (choice == 0) return;
            if (choice == 1) editBasicProfile(email);
            else if (choice == 2) changePhone(email);
            else if (choice == 3) AuthController.forgotPasswordFull(email);
        }
    }

    private static void editBasicProfile(String email) throws SQLException {
        String fullName = readValidated("New Full Name: ", Validators::name);
        String dob = readValidated("New Date of Birth: ", v -> {
            String err = Validators.dob(v);
            if (err.isEmpty() && !Validators.isAdult(v)) return "You must be 18 or older.";
            return err;
        });
        String emergencyContact = readValidated("New Emergency Contact (10 digits): ", Validators::phone10);

        UserDao.updateUserField(email, "full_name", fullName);
        UserDao.updateUserField(email, "dob", dob);
        UserDao.updateUserField(email, "emergency_contact", emergencyContact);

        BookingRules.addAudit(email, "Login", "Profile Updated", "");
        System.out.println("Profile updated.");
    }

    private static void changePhone(String email) throws SQLException {
        String newPhone = readValidated("New Phone Number (10 digits): ", Validators::phone10);
        System.out.println("Mock OTP sent to " + newPhone + " (use 123456).");
        String otp = readLine("Enter OTP: ");
        if (!MockApi.verifyOtp(otp)) {
            System.out.println("  ! Incorrect OTP. Phone number not changed.");
            return;
        }
        UserDao.updateUserField(email, "phone", newPhone);
        BookingRules.addAudit(email, "Login", "Phone Number Changed", newPhone);
        System.out.println("Phone number updated.");
    }

    private static void myBookings(String email) throws SQLException {
        while (true) {
            printHeader("My Bookings");
            List<Booking> bookings = BookingDao.findByUserEmail(email);
            List<String> ids = new ArrayList<>();
            int n = 1;
            for (Booking b : bookings) {
                ids.add(b.getId());
                System.out.println("\n" + n + ") " + b.getId() + " - " + b.getAircraftModel()
                        + " - " + b.getOrigin() + " -> " + b.getDestination());
                printLine("   Date", b.getDate());
                printLine("   Status", b.getStatus());
                printLine("   Total", fmtInr(b.getTotal()));
                n++;
            }
            if (ids.isEmpty()) {
                System.out.println("No bookings yet.");
                pause();
                return;
            }
            int choice = readIntInRange("\nOpen which booking? (0 to go back): ", 0, ids.size());
            if (choice == 0) return;
            openBooking(email, ids.get(choice - 1));
        }
    }

    private static void openBooking(String email, String bookingId) throws SQLException {
        Booking b = BookingDao.findById(bookingId);
        if (b == null) return;
        String status = b.getStatus();

        System.out.println("\nBooking " + bookingId + " - Status: " + status);
        List<String> actions = new ArrayList<>();
        if (BookingRules.isPayable(status)) actions.add("Pay Now");
        if (BookingRules.isCancellable(status)) actions.add("Cancel Booking");
        actions.add("Back");

        for (int i = 0; i < actions.size(); i++) System.out.println((i + 1) + ") " + actions.get(i));
        int choice = readIntInRange("Choose: ", 1, actions.size());
        String action = actions.get(choice - 1);

        if (action.equals("Pay Now")) PaymentController.payForBooking(email, bookingId);
        else if (action.equals("Cancel Booking")) cancelBooking(email, bookingId);
    }

    private static void cancelBooking(String email, String bookingId) throws SQLException {
        Booking b = BookingDao.findById(bookingId);
        if (b == null) return;
        String status = b.getStatus();
        long total = b.getTotal();

        if (!BookingRules.isCancellable(status)) {
            System.out.println("  ! Bookings with status \"" + status + "\" cannot be self-cancelled.");
            return;
        }

        Payment p = PaymentDao.findByBookingId(bookingId);
        long base = (p != null && "VERIFIED".equals(p.getStatus())) ? p.getAmount() : total;

        long fee = Math.round(base * 0.20);
        long refund = base - fee;

        printHeader("Cancel Booking " + bookingId);
        printLine("Cancellation Fee (20%)", fmtInr(fee));
        printLine("Refund Amount (80%)", fmtInr(refund));
        if (!readYesNo("Confirm cancellation?")) return;

        if (p != null && "VERIFIED".equals(p.getStatus())) {
            PaymentDao.updateRefund(p.getId(), fee, refund, "RETURNED");
        }

        BookingRules.releaseBookingResources(bookingId);
        BookingRules.voidUnsignedLease(bookingId);

        BookingDao.updateStatus(bookingId, "Cancelled");
        BookingDao.updateAssignedPilot(bookingId, null);
        BookingDao.updateAssignedCrew(bookingId, null);

        BookingRules.addAudit(email, "Booking", "Booking Cancelled", bookingId + " fee=" + fee + " refund=" + refund);
        BookingRules.addNotification(email, "Booking Cancelled",
                "Booking " + bookingId + " was cancelled. Refund of " + fmtInr(refund) + " will be processed.", "info");
        BookingRules.addNotification("admin", "Booking Cancelled", "Customer cancelled booking " + bookingId, "info");

        System.out.println("Booking cancelled.");
    }

    private static void notifications(String email) throws SQLException {
        List<com.jetlease.model.entity.Notification> ntfList = NotificationDao.findByUserEmail(email);
        CustomerView.renderNotifications(ntfList);
        if (!ntfList.isEmpty() && readYesNo("\nMark all as read?")) {
            NotificationDao.markAsRead(email);
        }
        pause();
    }

    private static void reportIssue(String email) throws SQLException {
        printHeader("Report an Issue");
        List<Booking> bookings = BookingDao.findByUserEmail(email);
        List<String> ids = new ArrayList<>();
        int n = 1;
        for (Booking b : bookings) {
            if ("Dispatched".equals(b.getStatus()) || "Completed".equals(b.getStatus())) {
                ids.add(b.getId());
                System.out.println(n + ") " + b.getId() + " - " + b.getAircraftModel() + " - " + b.getDate());
                n++;
            }
        }
        if (ids.isEmpty()) {
            System.out.println("You can only report an issue for a Dispatched or Completed flight, and you have none yet.");
            pause();
            return;
        }
        int choice = readIntInRange("Report an issue for which booking? (0 to cancel): ", 0, ids.size());
        if (choice == 0) return;
        String bookingId = ids.get(choice - 1);

        String subject = readValidated("Subject: ", v -> v.trim().isEmpty() ? "Subject is required." : "");
        String details = readValidated("Details: ", Validators::message);

        Report r = new Report();
        r.setId(IdGen.uid("RPT"));
        r.setBookingId(bookingId);
        r.setUserEmail(email);
        r.setSubject(subject);
        r.setDetails(details);
        r.setStatus("Open");
        r.setCreatedAt(IdGen.nowIso());

        ReportDao.save(r);

        BookingRules.addNotification("admin", "New Issue Report", "Report filed for booking " + bookingId + " by " + email, "warning");
        System.out.println("Report submitted. Our team will follow up.");
        pause();
    }
}
