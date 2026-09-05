package com.jetlease.model.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.jetlease.model.entity.Lease;

public class LeaseDao {

    private static Lease mapResultSet(ResultSet rs) throws SQLException {
        Lease l = new Lease();
        l.setId(rs.getString("id"));
        l.setBookingId(rs.getString("booking_id"));
        l.setUserEmail(rs.getString("user_email"));
        l.setStatus(rs.getString("status"));
        l.setSignedBy(rs.getString("signed_by"));
        l.setSignedDate(rs.getString("signed_date"));
        l.setApprovalDate(rs.getString("approval_date"));
        l.setCreatedAt(rs.getString("created_at"));
        return l;
    }

    public static List<Lease> findAll() throws SQLException {
        PreparedStatement ps = Db.getConnection().prepareStatement("SELECT * FROM leases ORDER BY created_at DESC");
        ResultSet rs = ps.executeQuery();
        List<Lease> list = new ArrayList<>();
        while (rs.next()) {
            list.add(mapResultSet(rs));
        }
        rs.close();
        ps.close();
        return list;
    }

    public static Lease findById(String id) throws SQLException {
        PreparedStatement ps = Db.getConnection().prepareStatement("SELECT * FROM leases WHERE id = ?");
        ps.setString(1, id);
        ResultSet rs = ps.executeQuery();
        Lease l = null;
        if (rs.next()) {
            l = mapResultSet(rs);
        }
        rs.close();
        ps.close();
        return l;
    }

    public static List<Lease> findByUserEmail(String email) throws SQLException {
        PreparedStatement ps = Db.getConnection().prepareStatement("SELECT * FROM leases WHERE user_email = ? ORDER BY created_at DESC");
        ps.setString(1, email);
        ResultSet rs = ps.executeQuery();
        List<Lease> list = new ArrayList<>();
        while (rs.next()) {
            list.add(mapResultSet(rs));
        }
        rs.close();
        ps.close();
        return list;
    }

    public static Lease findByBookingId(String bookingId) throws SQLException {
        PreparedStatement ps = Db.getConnection().prepareStatement("SELECT * FROM leases WHERE booking_id = ?");
        ps.setString(1, bookingId);
        ResultSet rs = ps.executeQuery();
        Lease l = null;
        if (rs.next()) {
            l = mapResultSet(rs);
        }
        rs.close();
        ps.close();
        return l;
    }

    public static void save(Lease l) throws SQLException {
        PreparedStatement ps = Db.getConnection().prepareStatement(
                "INSERT INTO leases (id,booking_id,user_email,status,signed_by,signed_date,approval_date,created_at) " +
                "VALUES (?,?,?,?,?,?,?,?)");
        ps.setString(1, l.getId());
        ps.setString(2, l.getBookingId());
        ps.setString(3, l.getUserEmail());
        ps.setString(4, l.getStatus());
        ps.setString(5, l.getSignedBy());
        ps.setString(6, l.getSignedDate());
        ps.setString(7, l.getApprovalDate());
        ps.setString(8, l.getCreatedAt());
        ps.executeUpdate();
        ps.close();
    }

    public static void signLease(String id, String signedBy, String signedDate) throws SQLException {
        PreparedStatement ps = Db.getConnection().prepareStatement("UPDATE leases SET status = 'Pending Admin Approval', signed_by = ?, signed_date = ? WHERE id = ?");
        ps.setString(1, signedBy);
        ps.setString(2, signedDate);
        ps.setString(3, id);
        ps.executeUpdate();
        ps.close();
    }

    public static void updateStatus(String id, String status, String approvalDate) throws SQLException {
        PreparedStatement ps = Db.getConnection().prepareStatement("UPDATE leases SET status = ?, approval_date = ? WHERE id = ?");
        ps.setString(1, status);
        ps.setString(2, approvalDate);
        ps.setString(3, id);
        ps.executeUpdate();
        ps.close();
    }
}
