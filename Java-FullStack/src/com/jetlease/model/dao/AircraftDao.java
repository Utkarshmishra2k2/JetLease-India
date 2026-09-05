package com.jetlease.model.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.jetlease.model.entity.Aircraft;

public class AircraftDao {

    private static Aircraft mapResultSet(ResultSet rs) throws SQLException {
        Aircraft a = new Aircraft();
        a.setId(rs.getString("id"));
        a.setReg(rs.getString("reg"));
        a.setModel(rs.getString("model"));
        a.setManufacturer(rs.getString("manufacturer"));
        a.setCategory(rs.getString("category"));
        a.setCapacity(rs.getInt("capacity"));
        a.setSpeed(rs.getInt("speed"));
        a.setRangeKm(rs.getInt("range_km"));
        a.setHourlyRate(rs.getLong("hourly_rate"));
        a.setStatus(rs.getString("status"));
        a.setTypeRating(rs.getString("type_rating"));
        return a;
    }

    public static List<Aircraft> findAll() throws SQLException {
        PreparedStatement ps = Db.getConnection().prepareStatement("SELECT * FROM aircraft ORDER BY model");
        ResultSet rs = ps.executeQuery();
        List<Aircraft> list = new ArrayList<>();
        while (rs.next()) {
            list.add(mapResultSet(rs));
        }
        rs.close();
        ps.close();
        return list;
    }

    public static Aircraft findById(String id) throws SQLException {
        PreparedStatement ps = Db.getConnection().prepareStatement("SELECT * FROM aircraft WHERE id = ?");
        ps.setString(1, id);
        ResultSet rs = ps.executeQuery();
        Aircraft a = null;
        if (rs.next()) {
            a = mapResultSet(rs);
        }
        rs.close();
        ps.close();
        return a;
    }

    public static List<Aircraft> findAvailable(String category, int minCapacity) throws SQLException {
        String sql = "SELECT * FROM aircraft WHERE status = 'Available' AND capacity >= ?";
        if (category != null) sql += " AND category = ?";
        sql += " ORDER BY hourly_rate";

        PreparedStatement ps = Db.getConnection().prepareStatement(sql);
        ps.setInt(1, minCapacity);
        if (category != null) ps.setString(2, category);

        ResultSet rs = ps.executeQuery();
        List<Aircraft> list = new ArrayList<>();
        while (rs.next()) {
            list.add(mapResultSet(rs));
        }
        rs.close();
        ps.close();
        return list;
    }

    public static void save(Aircraft a) throws SQLException {
        PreparedStatement ps = Db.getConnection().prepareStatement(
                "INSERT INTO aircraft (id,reg,model,manufacturer,category,capacity,speed,range_km,hourly_rate,status,type_rating) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?)");
        ps.setString(1, a.getId());
        ps.setString(2, a.getReg());
        ps.setString(3, a.getModel());
        ps.setString(4, a.getManufacturer());
        ps.setString(5, a.getCategory());
        ps.setInt(6, a.getCapacity());
        ps.setInt(7, a.getSpeed());
        ps.setInt(8, a.getRangeKm());
        ps.setLong(9, a.getHourlyRate());
        ps.setString(10, a.getStatus());
        ps.setString(11, a.getTypeRating());
        ps.executeUpdate();
        ps.close();
    }

    public static void updateStatus(String id, String status) throws SQLException {
        PreparedStatement ps = Db.getConnection().prepareStatement("UPDATE aircraft SET status = ? WHERE id = ?");
        ps.setString(1, status);
        ps.setString(2, id);
        ps.executeUpdate();
        ps.close();
    }

    public static void updateHourlyRate(String id, long rate) throws SQLException {
        PreparedStatement ps = Db.getConnection().prepareStatement("UPDATE aircraft SET hourly_rate = ? WHERE id = ?");
        ps.setLong(1, rate);
        ps.setString(2, id);
        ps.executeUpdate();
        ps.close();
    }

    public static void delete(String id) throws SQLException {
        PreparedStatement ps = Db.getConnection().prepareStatement("DELETE FROM aircraft WHERE id = ?");
        ps.setString(1, id);
        ps.executeUpdate();
        ps.close();
    }
}
