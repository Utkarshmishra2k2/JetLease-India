package com.jetlease.model.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.jetlease.model.entity.Crew;

public class CrewDao {

    private static Crew mapResultSet(ResultSet rs) throws SQLException {
        Crew c = new Crew();
        c.setId(rs.getString("id"));
        c.setName(rs.getString("name"));
        c.setRole(rs.getString("role"));
        c.setDutyHours(rs.getInt("duty_hours"));
        c.setRemainingHours(rs.getInt("remaining_hours"));
        c.setAvailable(rs.getInt("available") == 1);
        return c;
    }

    public static List<Crew> findAll() throws SQLException {
        PreparedStatement ps = Db.getConnection().prepareStatement("SELECT * FROM crew ORDER BY name");
        ResultSet rs = ps.executeQuery();
        List<Crew> list = new ArrayList<>();
        while (rs.next()) {
            list.add(mapResultSet(rs));
        }
        rs.close();
        ps.close();
        return list;
    }

    public static Crew findById(String id) throws SQLException {
        PreparedStatement ps = Db.getConnection().prepareStatement("SELECT * FROM crew WHERE id = ?");
        ps.setString(1, id);
        ResultSet rs = ps.executeQuery();
        Crew c = null;
        if (rs.next()) {
            c = mapResultSet(rs);
        }
        rs.close();
        ps.close();
        return c;
    }

    public static void save(Crew c) throws SQLException {
        PreparedStatement ps = Db.getConnection().prepareStatement(
                "INSERT INTO crew (id,name,role,duty_hours,remaining_hours,available) " +
                "VALUES (?,?,?,?,?,?)");
        ps.setString(1, c.getId());
        ps.setString(2, c.getName());
        ps.setString(3, c.getRole());
        ps.setInt(4, c.getDutyHours());
        ps.setInt(5, c.getRemainingHours());
        ps.setInt(6, c.isAvailable() ? 1 : 0);
        ps.executeUpdate();
        ps.close();
    }

    public static void updateAvailability(String id, boolean available) throws SQLException {
        PreparedStatement ps = Db.getConnection().prepareStatement("UPDATE crew SET available = ? WHERE id = ?");
        ps.setInt(1, available ? 1 : 0);
        ps.setString(2, id);
        ps.executeUpdate();
        ps.close();
    }
}
