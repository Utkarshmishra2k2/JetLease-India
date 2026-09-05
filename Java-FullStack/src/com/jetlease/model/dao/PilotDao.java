package com.jetlease.model.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.jetlease.model.entity.Pilot;

public class PilotDao {

    private static Pilot mapResultSet(ResultSet rs) throws SQLException {
        Pilot p = new Pilot();
        p.setId(rs.getString("id"));
        p.setName(rs.getString("name"));
        p.setLicenseNumber(rs.getString("license_number"));
        p.setFlyingHours(rs.getInt("flying_hours"));
        p.setRemainingHours(rs.getInt("remaining_hours"));
        p.setTypeRatings(rs.getString("type_ratings"));
        p.setCertifications(rs.getString("certifications"));
        p.setAvailable(rs.getInt("available") == 1);
        return p;
    }

    public static List<Pilot> findAll() throws SQLException {
        PreparedStatement ps = Db.getConnection().prepareStatement("SELECT * FROM pilots ORDER BY name");
        ResultSet rs = ps.executeQuery();
        List<Pilot> list = new ArrayList<>();
        while (rs.next()) {
            list.add(mapResultSet(rs));
        }
        rs.close();
        ps.close();
        return list;
    }

    public static Pilot findById(String id) throws SQLException {
        PreparedStatement ps = Db.getConnection().prepareStatement("SELECT * FROM pilots WHERE id = ?");
        ps.setString(1, id);
        ResultSet rs = ps.executeQuery();
        Pilot p = null;
        if (rs.next()) {
            p = mapResultSet(rs);
        }
        rs.close();
        ps.close();
        return p;
    }

    public static void save(Pilot p) throws SQLException {
        PreparedStatement ps = Db.getConnection().prepareStatement(
                "INSERT INTO pilots (id,name,license_number,flying_hours,remaining_hours,type_ratings,certifications,available) " +
                "VALUES (?,?,?,?,?,?,?,?)");
        ps.setString(1, p.getId());
        ps.setString(2, p.getName());
        ps.setString(3, p.getLicenseNumber());
        ps.setInt(4, p.getFlyingHours());
        ps.setInt(5, p.getRemainingHours());
        ps.setString(6, p.getTypeRatings());
        ps.setString(7, p.getCertifications());
        ps.setInt(8, p.isAvailable() ? 1 : 0);
        ps.executeUpdate();
        ps.close();
    }

    public static void updateAvailability(String id, boolean available) throws SQLException {
        PreparedStatement ps = Db.getConnection().prepareStatement("UPDATE pilots SET available = ? WHERE id = ?");
        ps.setInt(1, available ? 1 : 0);
        ps.setString(2, id);
        ps.executeUpdate();
        ps.close();
    }
}
