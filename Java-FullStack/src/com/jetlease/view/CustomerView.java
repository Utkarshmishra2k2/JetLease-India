package com.jetlease.view;

import java.util.List;
import static com.jetlease.view.ConsoleUtil.*;

import com.jetlease.model.entity.Booking;
import com.jetlease.model.entity.Notification;
import com.jetlease.model.entity.User;

public class CustomerView {

    public static void displayDashboardMenu(String userName) {
        printHeader("Dashboard - " + userName);
        System.out.println("1) Overview");
        System.out.println("2) My Profile");
        System.out.println("3) Book a New Flight");
        System.out.println("4) My Bookings");
        System.out.println("5) My Payments");
        System.out.println("6) My Notifications");
        System.out.println("7) My Leases");
        System.out.println("8) Report an Issue");
        System.out.println("0) Logout");
    }

    public static void renderOverview(User user, int totalBookings, int upcomingCount, List<Booking> upcomingFlights) {
        printHeader("Overview");
        printLine("Membership Tier", user.getMembership());
        printLine("Loyalty Points", user.getLoyaltyPoints());
        printLine("Total Bookings", totalBookings);
        printLine("Upcoming/Active Bookings", upcomingCount);

        System.out.println("\nUpcoming Flights:");
        if (upcomingFlights.isEmpty()) {
            System.out.println("  None.");
        } else {
            for (Booking b : upcomingFlights) {
                System.out.println("  " + b.getId() + " - " + b.getOrigin() + " -> " + b.getDestination()
                        + " on " + b.getDate() + " - " + b.getStatus());
            }
        }
        pause();
    }

    public static void renderProfile(User user) {
        printHeader("My Profile");
        printLine("Full Name", user.getFullName());
        printLine("Email", user.getEmail());
        printLine("Phone", user.getPhone());
        printLine("Date of Birth", user.getDob());
        printLine("Emergency Contact", user.getEmergencyContact());
        printLine("Country", user.getCountry());
        printLine("Membership", user.getMembership());
        printLine("Loyalty Points", user.getLoyaltyPoints());
        System.out.println("\n1) Edit Name/DOB/Emergency Contact  2) Change Phone Number  3) Forgot/Reset Password  0) Back");
    }

    public static void renderNotifications(List<Notification> notifications) {
        printHeader("My Notifications");
        if (notifications.isEmpty()) {
            System.out.println("No notifications.");
        } else {
            for (Notification n : notifications) {
                String mark = n.isRead() ? " " : "*";
                System.out.println("\n" + mark + " [" + n.getType() + "] " + n.getTitle());
                System.out.println("   " + n.getMessage());
                System.out.println("   " + n.getCreatedAt());
            }
        }
    }
}
