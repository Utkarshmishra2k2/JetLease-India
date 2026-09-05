package com.jetlease.model.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.jetlease.model.entity.ContactMessage;

public class ContactMessageDao {

    private static ContactMessage mapResultSet(ResultSet rs) throws SQLException {
        ContactMessage c = new ContactMessage();
        c.setId(rs.getString("id"));
        c.setName(rs.getString("name"));
        c.setPhone(rs.getString("phone"));
        c.setEmail(rs.getString("email"));
        c.setMessage(rs.getString("message"));
        c.setStatus(rs.getString("status"));
        c.setCreatedAt(rs.getString("created_at"));
        return c;
    }

    public static List<ContactMessage> findAll() throws SQLException {
        PreparedStatement ps = Db.getConnection().prepareStatement("SELECT * FROM contact_messages ORDER BY created_at DESC");
        ResultSet rs = ps.executeQuery();
        List<ContactMessage> list = new ArrayList<>();
        while (rs.next()) {
            list.add(mapResultSet(rs));
        }
        rs.close();
        ps.close();
        return list;
    }

    public static ContactMessage findById(String id) throws SQLException {
        PreparedStatement ps = Db.getConnection().prepareStatement("SELECT * FROM contact_messages WHERE id = ?");
        ps.setString(1, id);
        ResultSet rs = ps.executeQuery();
        ContactMessage c = null;
        if (rs.next()) {
            c = mapResultSet(rs);
        }
        rs.close();
        ps.close();
        return c;
    }

    public static void save(ContactMessage c) throws SQLException {
        PreparedStatement ps = Db.getConnection().prepareStatement(
                "INSERT INTO contact_messages (id,name,phone,email,message,status,created_at) VALUES (?,?,?,?,?,?,?)");
        ps.setString(1, c.getId());
        ps.setString(2, c.getName());
        ps.setString(3, c.getPhone());
        ps.setString(4, c.getEmail());
        ps.setString(5, c.getMessage());
        ps.setString(6, c.getStatus());
        ps.setString(7, c.getCreatedAt());
        ps.executeUpdate();
        ps.close();
    }

    public static void updateStatus(String id, String status) throws SQLException {
        PreparedStatement ps = Db.getConnection().prepareStatement("UPDATE contact_messages SET status = ? WHERE id = ?");
        ps.setString(1, status);
        ps.setString(2, id);
        ps.executeUpdate();
        ps.close();
    }
}
