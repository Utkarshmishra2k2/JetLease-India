package com.jetlease.model.service;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;

import com.jetlease.model.dao.BookingDao;
import com.jetlease.model.dao.LeaseDao;
import com.jetlease.view.ConsoleUtil;

public class LeaseService {

    public static String buildLeaseText(String leaseId, String bookingId, String userEmail, String aircraftModel,
                                         String route, String date, long total, String status,
                                         String signedBy, String signedDate) {
        StringBuilder sb = new StringBuilder();
        sb.append("AIRCRAFT LEASE AGREEMENT\n");
        sb.append("Lease ID: ").append(leaseId).append("\n");
        sb.append("Booking Reference: ").append(bookingId).append("\n");
        sb.append("Lessee: ").append(userEmail).append("\n");
        sb.append("Aircraft: ").append(aircraftModel).append("\n");
        sb.append("Route: ").append(route).append("\n");
        sb.append("Date of Flight: ").append(date).append("\n");
        sb.append("Total Charter Value: ").append(ConsoleUtil.fmtInr(total)).append("\n\n");
        sb.append("This agreement confirms the terms under which JetLease India Charters Pvt Ltd\n");
        sb.append("leases the above aircraft to the lessee for the stated route and date, subject\n");
        sb.append("to all applicable DGCA regulations and the JetLease Terms of Service.\n\n");
        sb.append("Status: ").append(status).append("\n");
        if (signedBy != null) sb.append("Signed By: ").append(signedBy).append(" on ").append(signedDate).append("\n");
        return sb.toString();
    }

    public static void signLease(String userEmail, String leaseId, String bookingId, String legalName) throws SQLException {
        String today = IdGen.todayIso();
        LeaseDao.signLease(leaseId, legalName, today);
        BookingDao.updateStatus(bookingId, "Lease Signed");

        BookingRules.addAudit(userEmail, "Lease", "Lease Signed", leaseId);
        BookingRules.addNotification("admin", "Lease Signed", "Lease " + leaseId + " was signed by " + userEmail + ". Awaiting approval.", "info");
        BookingRules.addNotification(userEmail, "Lease Signed", "You signed lease " + leaseId + ". It is now awaiting admin approval.", "success");
    }

    public static boolean exportLease(String leaseId, String contract) {
        try {
            String fileName = leaseId + ".txt";
            FileWriter fw = new FileWriter(fileName);
            fw.write(contract);
            fw.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
