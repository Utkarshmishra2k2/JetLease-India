package com.jetlease.model.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.jetlease.model.entity.AuditLog;

public class AuditLogDao {

    private static AuditLog mapResultSet(ResultSet rs) throws SQLException {
        AuditLog a = new AuditLog();
        a.setId(rs.getString("id"));
        a.setActor(rs.getString("actor"));
        a.setCategory(rs.getString("category"));
        a.setAction(rs.getString("action"));
        a.setDetails(rs.getString("details"));
        a.setTimestamp(rs.getString("timestamp"));
        return a;
    }

    public static List<AuditLog> findAll() throws SQLException {
        PreparedStatement ps = Db.getConnection().prepareStatement("SELECT * FROM audit_log ORDER BY timestamp DESC");
        ResultSet rs = ps.executeQuery();
        List<AuditLog> list = new ArrayList<>();
        while (rs.next()) {
            list.add(mapResultSet(rs));
        }
        rs.close();
        ps.close();
        return list;
    }

    public static void save(AuditLog a) throws SQLException {
        PreparedStatement ps = Db.getConnection().prepareStatement(
                "INSERT INTO audit_log (id,actor,category,action,details,timestamp) VALUES (?,?,?,?,?,?)");
        ps.setString(1, a.getId());
        ps.setString(2, a.getActor());
        ps.setString(3, a.getCategory());
        ps.setString(4, a.getAction());
        ps.setString(5, a.getDetails());
        ps.setString(6, a.getTimestamp());
        ps.executeUpdate();
        ps.close();
    }
}
