package com.jetlease.model.dao;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.jetlease.model.entity.Booking;

public class BookingDao {

    private static Booking mapResultSet(ResultSet rs) throws SQLException {
        Booking b = new Booking();
        b.setId(rs.getString("id"));
        b.setUserEmail(rs.getString("user_email"));
        b.setType(rs.getString("type"));
        b.setTripType(rs.getString("trip_type"));
        b.setOrigin(rs.getString("origin"));
        b.setDestination(rs.getString("destination"));
        b.setDate(rs.getString("date"));
        b.setTime(rs.getString("time"));
        b.setReturnDate(rs.getString("return_date"));
        b.setReturnTime(rs.getString("return_time"));
        b.setPax(rs.getInt("pax"));
        b.setAircraftId(rs.getString("aircraft_id"));
        b.setAircraftModel(rs.getString("aircraft_model"));
        b.setSelfFly(rs.getInt("self_fly") == 1);
        b.setLicenseNumber(rs.getString("license_number"));
        b.setLicenseClass(rs.getString("license_class"));
        b.setFlyingHours(rs.getInt("flying_hours"));
        b.setCertificateFileName(rs.getString("certificate_file_name"));
        b.setDgcaDeclaration(rs.getInt("dgca_declaration") == 1);
        b.setLicenseVerified(rs.getInt("license_verified") == 1);
        b.setHours(rs.getDouble("hours"));
        b.setAircraftCost(rs.getLong("aircraft_cost"));
        b.setPilotCost(rs.getLong("pilot_cost"));
        b.setCrewCost(rs.getLong("crew_cost"));
        b.setAirportCharges(rs.getLong("airport_charges"));
        b.setFuelSurcharge(rs.getLong("fuel_surcharge"));
        b.setGst(rs.getLong("gst"));
        b.setTotal(rs.getLong("total"));
        b.setStatus(rs.getString("status"));
        b.setAssignedPilotId(rs.getString("assigned_pilot_id"));
        b.setAssignedCrewIds(rs.getString("assigned_crew_ids"));
        b.setCreatedAt(rs.getString("created_at"));
        return b;
    }

    public static List<Booking> findAll() throws SQLException {
        PreparedStatement ps = Db.getConnection().prepareStatement("SELECT * FROM bookings ORDER BY created_at DESC");
        ResultSet rs = ps.executeQuery();
        List<Booking> list = new ArrayList<>();
        while (rs.next()) {
            list.add(mapResultSet(rs));
        }
        rs.close();
        ps.close();
        return list;
    }

    public static Booking findById(String id) throws SQLException {
        PreparedStatement ps = Db.getConnection().prepareStatement("SELECT * FROM bookings WHERE id = ?");
        ps.setString(1, id);
        ResultSet rs = ps.executeQuery();
        Booking b = null;
        if (rs.next()) {
            b = mapResultSet(rs);
        }
        rs.close();
        ps.close();
        return b;
    }

    public static List<Booking> findByUserEmail(String email) throws SQLException {
        PreparedStatement ps = Db.getConnection().prepareStatement("SELECT * FROM bookings WHERE user_email = ? ORDER BY created_at DESC");
        ps.setString(1, email);
        ResultSet rs = ps.executeQuery();
        List<Booking> list = new ArrayList<>();
        while (rs.next()) {
            list.add(mapResultSet(rs));
        }
        rs.close();
        ps.close();
        return list;
    }

    public static void save(Booking b) throws SQLException {
        String sql = "INSERT INTO bookings (id,user_email,type,trip_type,origin,destination,date,time,return_date,return_time," +
                "pax,aircraft_id,aircraft_model,self_fly,license_number,license_class,flying_hours,certificate_file_name," +
                "dgca_declaration,license_verified,hours,aircraft_cost,pilot_cost,crew_cost,airport_charges,fuel_surcharge,gst,total,status,assigned_pilot_id,assigned_crew_ids,created_at) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        PreparedStatement ps = Db.getConnection().prepareStatement(sql);
        ps.setString(1, b.getId());
        ps.setString(2, b.getUserEmail());
        ps.setString(3, b.getType());
        ps.setString(4, b.getTripType());
        ps.setString(5, b.getOrigin());
        ps.setString(6, b.getDestination());
        ps.setString(7, b.getDate());
        ps.setString(8, b.getTime());
        ps.setString(9, b.getReturnDate());
        ps.setString(10, b.getReturnTime());
        ps.setInt(11, b.getPax());
        ps.setString(12, b.getAircraftId());
        ps.setString(13, b.getAircraftModel());
        ps.setInt(14, b.isSelfFly() ? 1 : 0);
        ps.setString(15, b.getLicenseNumber());
        ps.setString(16, b.getLicenseClass());
        ps.setInt(17, b.getFlyingHours());
        ps.setString(18, b.getCertificateFileName());
        ps.setInt(19, b.isDgcaDeclaration() ? 1 : 0);
        ps.setInt(20, b.isLicenseVerified() ? 1 : 0);
        ps.setDouble(21, b.getHours());
        ps.setLong(22, b.getAircraftCost());
        ps.setLong(23, b.getPilotCost());
        ps.setLong(24, b.getCrewCost());
        ps.setLong(25, b.getAirportCharges());
        ps.setLong(26, b.getFuelSurcharge());
        ps.setLong(27, b.getGst());
        ps.setLong(28, b.getTotal());
        ps.setString(29, b.getStatus());
        ps.setString(30, b.getAssignedPilotId());
        ps.setString(31, b.getAssignedCrewIds());
        ps.setString(32, b.getCreatedAt());
        ps.executeUpdate();
        ps.close();
    }

    public static void updateStatus(String id, String status) throws SQLException {
        PreparedStatement ps = Db.getConnection().prepareStatement("UPDATE bookings SET status = ? WHERE id = ?");
        ps.setString(1, status);
        ps.setString(2, id);
        ps.executeUpdate();
        ps.close();
    }

    public static void updateAssignedPilot(String bookingId, String pilotId) throws SQLException {
        PreparedStatement ps = Db.getConnection().prepareStatement("UPDATE bookings SET assigned_pilot_id = ? WHERE id = ?");
        ps.setString(1, pilotId);
        ps.setString(2, bookingId);
        ps.executeUpdate();
        ps.close();
    }

    public static void updateAssignedCrew(String bookingId, String crewIds) throws SQLException {
        PreparedStatement ps = Db.getConnection().prepareStatement("UPDATE bookings SET assigned_crew_ids = ? WHERE id = ?");
        ps.setString(1, crewIds);
        ps.setString(2, bookingId);
        ps.executeUpdate();
        ps.close();
    }
}
