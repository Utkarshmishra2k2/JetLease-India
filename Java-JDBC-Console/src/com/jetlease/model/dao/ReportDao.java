package com.jetlease.model.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.jetlease.model.entity.Report;

public class ReportDao {

    private static Report mapResultSet(ResultSet rs) throws SQLException {
        Report r = new Report();
        r.setId(rs.getString("id"));
        r.setBookingId(rs.getString("booking_id"));
        r.setUserEmail(rs.getString("user_email"));
        r.setSubject(rs.getString("subject"));
        r.setDetails(rs.getString("details"));
        r.setStatus(rs.getString("status"));
        r.setCreatedAt(rs.getString("created_at"));
        return r;
    }

    public static List<Report> findAll() throws SQLException {
        PreparedStatement ps = Db.getConnection().prepareStatement("SELECT * FROM reports ORDER BY created_at DESC");
        ResultSet rs = ps.executeQuery();
        List<Report> list = new ArrayList<>();
        while (rs.next()) {
            list.add(mapResultSet(rs));
        }
        rs.close();
        ps.close();
        return list;
    }

    public static Report findById(String id) throws SQLException {
        PreparedStatement ps = Db.getConnection().prepareStatement("SELECT * FROM reports WHERE id = ?");
        ps.setString(1, id);
        ResultSet rs = ps.executeQuery();
        Report r = null;
        if (rs.next()) {
            r = mapResultSet(rs);
        }
        rs.close();
        ps.close();
        return r;
    }

    public static List<Report> findByUserEmail(String email) throws SQLException {
        PreparedStatement ps = Db.getConnection().prepareStatement("SELECT * FROM reports WHERE user_email = ? ORDER BY created_at DESC");
        ps.setString(1, email);
        ResultSet rs = ps.executeQuery();
        List<Report> list = new ArrayList<>();
        while (rs.next()) {
            list.add(mapResultSet(rs));
        }
        rs.close();
        ps.close();
        return list;
    }

    public static void save(Report r) throws SQLException {
        PreparedStatement ps = Db.getConnection().prepareStatement(
                "INSERT INTO reports (id,booking_id,user_email,subject,details,status,created_at) VALUES (?,?,?,?,?,?,?)");
        ps.setString(1, r.getId());
        ps.setString(2, r.getBookingId());
        ps.setString(3, r.getUserEmail());
        ps.setString(4, r.getSubject());
        ps.setString(5, r.getDetails());
        ps.setString(6, r.getStatus());
        ps.setString(7, r.getCreatedAt());
        ps.executeUpdate();
        ps.close();
    }

    public static void updateStatus(String id, String status) throws SQLException {
        PreparedStatement ps = Db.getConnection().prepareStatement("UPDATE reports SET status = ? WHERE id = ?");
        ps.setString(1, status);
        ps.setString(2, id);
        ps.executeUpdate();
        ps.close();
    }
}
