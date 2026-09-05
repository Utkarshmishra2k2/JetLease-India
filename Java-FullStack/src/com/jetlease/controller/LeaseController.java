package com.jetlease.controller;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.jetlease.view.ConsoleUtil.*;

import com.jetlease.model.dao.BookingDao;
import com.jetlease.model.dao.LeaseDao;
import com.jetlease.model.entity.Booking;
import com.jetlease.model.entity.Lease;
import com.jetlease.model.service.LeaseService;
import com.jetlease.view.LeaseView;

public class LeaseController {

    public static void listMyLeases(String userEmail) throws SQLException {
        LeaseView.displayMyLeasesHeader();
        List<Lease> leases = LeaseDao.findByUserEmail(userEmail);
        if (leases.isEmpty()) {
            System.out.println("No lease agreements yet.");
            pause();
            return;
        }

        int n = 1;
        List<String> ids = new ArrayList<>();
        for (Lease l : leases) {
            ids.add(l.getId());
            Booking b = BookingDao.findById(l.getBookingId());
            String aircraftModel = b != null ? b.getAircraftModel() : "N/A";
            System.out.println("\n" + n + ") Lease " + l.getId() + " - Booking " + l.getBookingId()
                    + " - " + aircraftModel + " - Status: " + l.getStatus());
            n++;
        }

        int choice = readIntInRange("\nOpen which lease? (0 to go back): ", 0, ids.size());
        if (choice == 0) return;
        openLease(userEmail, ids.get(choice - 1));
    }

    private static void openLease(String userEmail, String leaseId) throws SQLException {
        while (true) {
            Lease l = LeaseDao.findById(leaseId);
            if (l == null) return;
            Booking b = BookingDao.findById(l.getBookingId());
            if (b == null) return;

            String route = b.getOrigin() + " to " + b.getDestination();
            String contract = LeaseService.buildLeaseText(l.getId(), b.getId(), userEmail,
                    b.getAircraftModel(), route, b.getDate(), b.getTotal(), l.getStatus(),
                    l.getSignedBy(), l.getSignedDate());

            LeaseView.renderLeaseText(l.getId(), l.getStatus(), contract);

            System.out.println("\n1) Sign this lease   2) Export to a text file   0) Back");
            int choice = readIntInRange("Choose: ", 0, 2);
            if (choice == 0) return;
            if (choice == 1) {
                if (!l.getStatus().equals("Sent")) {
                    System.out.println("  ! Only a lease with status \"Sent\" can be signed.");
                    continue;
                }
                String legalName = readValidated("Type your full legal name to sign: ", v -> v.trim().length() < 3 ? "Enter your full legal name." : "");
                LeaseService.signLease(userEmail, l.getId(), b.getId(), legalName);
                System.out.println("Lease signed successfully.");
            } else if (choice == 2) {
                boolean ok = LeaseService.exportLease(l.getId(), contract);
                if (ok) {
                    System.out.println("Lease exported to " + l.getId() + ".txt in the current folder.");
                } else {
                    System.out.println("  ! Could not write file.");
                }
            }
        }
    }
}
