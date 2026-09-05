package com.jetlease.model.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import com.jetlease.model.dao.Db;

public class BookingRules {

    public static final List<String> ACTIVE_STATUSES = Arrays.asList(
            "Pending Payment", "Pending Verification", "Payment Verified", "Lease Pending",
            "Lease Signed", "Approved", "Dispatched");

    public static final List<String> CANCELLABLE_STATUSES = Arrays.asList("Payment Verified", "Lease Pending");

    public static final List<String> PAYABLE_STATUSES = Arrays.asList("Pending Payment", "Payment Rejected");

    public static final List<String> ENDED_STATUSES = Arrays.asList("Completed", "Cancelled", "Rejected");

    public static boolean isActive(String status) { return ACTIVE_STATUSES.contains(status); }
    public static boolean isCancellable(String status) { return CANCELLABLE_STATUSES.contains(status); }
    public static boolean isPayable(String status) { return PAYABLE_STATUSES.contains(status); }
    public static boolean isUpcoming(String status) { return !ENDED_STATUSES.contains(status); }

    public static void addAudit(String actor, String category, String action, String details) throws SQLException {
        PreparedStatement ps = Db.getConnection().prepareStatement(
                "INSERT INTO audit_log (id, actor, category, action, details, timestamp) VALUES (?,?,?,?,?,?)");
        ps.setString(1, IdGen.uid("AUD"));
        ps.setString(2, actor);
        ps.setString(3, category);
        ps.setString(4, action);
        ps.setString(5, details == null ? "" : details);
        ps.setString(6, IdGen.nowIso());
        ps.executeUpdate();
        ps.close();
    }

    public static void addNotification(String userEmail, String title, String message, String type) throws SQLException {
        PreparedStatement ps = Db.getConnection().prepareStatement(
                "INSERT INTO notifications (id, user_email, title, message, type, is_read, created_at) VALUES (?,?,?,?,?,?,?)");
        ps.setString(1, IdGen.uid("NTF"));
        ps.setString(2, userEmail);
        ps.setString(3, title);
        ps.setString(4, message);
        ps.setString(5, type);
        ps.setInt(6, 0);
        ps.setString(7, IdGen.nowIso());
        ps.executeUpdate();
        ps.close();
    }

    public static void releaseBookingResources(String bookingId) throws SQLException {
        Connection conn = Db.getConnection();

        PreparedStatement getB = conn.prepareStatement("SELECT * FROM bookings WHERE id = ?");
        getB.setString(1, bookingId);
        ResultSet rs = getB.executeQuery();
        if (!rs.next()) { rs.close(); getB.close(); return; }
        String aircraftId = rs.getString("aircraft_id");
        String pilotId = rs.getString("assigned_pilot_id");
        String crewIdsCsv = rs.getString("assigned_crew_ids");
        double hours = rs.getDouble("hours");
        rs.close();
        getB.close();

        PreparedStatement freeAircraft = conn.prepareStatement(
                "UPDATE aircraft SET status = 'Available' WHERE id = ? AND status = 'Booked'");
        freeAircraft.setString(1, aircraftId);
        freeAircraft.executeUpdate();
        freeAircraft.close();

        if (pilotId != null && !pilotId.isEmpty()) {
            PreparedStatement refundPilot = conn.prepareStatement(
                    "UPDATE pilots SET remaining_hours = remaining_hours + ? WHERE id = ?");
            refundPilot.setDouble(1, hours);
            refundPilot.setString(2, pilotId);
            refundPilot.executeUpdate();
            refundPilot.close();
        }
        if (crewIdsCsv != null && !crewIdsCsv.isEmpty()) {
            for (String cid : crewIdsCsv.split(",")) {
                PreparedStatement refundCrew = conn.prepareStatement(
                        "UPDATE crew SET remaining_hours = remaining_hours + ? WHERE id = ?");
                refundCrew.setDouble(1, hours);
                refundCrew.setString(2, cid);
                refundCrew.executeUpdate();
                refundCrew.close();
            }
        }
    }

    public static void voidUnsignedLease(String bookingId) throws SQLException {
        PreparedStatement ps = Db.getConnection().prepareStatement(
                "UPDATE leases SET status = 'Unenforceable' WHERE booking_id = ? AND status = 'Sent'");
        ps.setString(1, bookingId);
        ps.executeUpdate();
        ps.close();
    }

    public static void ensureLeaseForBooking(String bookingId, String userEmail) throws SQLException {
        Connection conn = Db.getConnection();
        PreparedStatement check = conn.prepareStatement("SELECT id FROM leases WHERE booking_id = ?");
        check.setString(1, bookingId);
        ResultSet rs = check.executeQuery();
        boolean exists = rs.next();
        rs.close();
        check.close();
        if (exists) return;

        PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO leases (id, booking_id, user_email, status, signed_by, signed_date, approval_date, created_at) " +
                "VALUES (?,?,?,?,?,?,?,?)");
        ps.setString(1, IdGen.uid("LSE"));
        ps.setString(2, bookingId);
        ps.setString(3, userEmail);
        ps.setString(4, "Sent");
        ps.setString(5, null);
        ps.setString(6, null);
        ps.setString(7, null);
        ps.setString(8, IdGen.nowIso());
        ps.executeUpdate();
        ps.close();

        addNotification(userEmail, "Lease Agreement Ready",
                "Your lease agreement for booking " + bookingId + " is ready to review and sign.", "info");
    }
}
