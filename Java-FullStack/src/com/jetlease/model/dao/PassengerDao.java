package com.jetlease.model.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.jetlease.model.entity.Passenger;

public class PassengerDao {

    private static Passenger mapResultSet(ResultSet rs) throws SQLException {
        Passenger p = new Passenger();
        p.setId(rs.getInt("id"));
        p.setBookingId(rs.getString("booking_id"));
        p.setName(rs.getString("name"));
        p.setDob(rs.getString("dob"));
        p.setGender(rs.getString("gender"));
        p.setAadhaar(rs.getString("aadhaar"));
        p.setVerificationStatus(rs.getString("verification_status"));
        p.setNoAadhaar(rs.getInt("no_aadhaar") == 1);
        p.setAltDocumentId(rs.getString("alt_document_id"));
        return p;
    }

    public static List<Passenger> findByBookingId(String bookingId) throws SQLException {
        PreparedStatement ps = Db.getConnection().prepareStatement("SELECT * FROM passengers WHERE booking_id = ?");
        ps.setString(1, bookingId);
        ResultSet rs = ps.executeQuery();
        List<Passenger> list = new ArrayList<>();
        while (rs.next()) {
            list.add(mapResultSet(rs));
        }
        rs.close();
        ps.close();
        return list;
    }

    public static void save(Passenger p) throws SQLException {
        PreparedStatement ps = Db.getConnection().prepareStatement(
                "INSERT INTO passengers (booking_id,name,dob,gender,aadhaar,verification_status,no_aadhaar,alt_document_id) " +
                "VALUES (?,?,?,?,?,?,?,?)");
        ps.setString(1, p.getBookingId());
        ps.setString(2, p.getName());
        ps.setString(3, p.getDob());
        ps.setString(4, p.getGender());
        ps.setString(5, p.getAadhaar());
        ps.setString(6, p.getVerificationStatus());
        ps.setInt(7, p.isNoAadhaar() ? 1 : 0);
        ps.setString(8, p.getAltDocumentId());
        ps.executeUpdate();
        ps.close();
    }
}
