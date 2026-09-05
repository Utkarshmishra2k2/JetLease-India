package com.jetlease.model.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.jetlease.model.entity.Notification;

public class NotificationDao {

    private static Notification mapResultSet(ResultSet rs) throws SQLException {
        Notification n = new Notification();
        n.setId(rs.getString("id"));
        n.setUserEmail(rs.getString("user_email"));
        n.setTitle(rs.getString("title"));
        n.setMessage(rs.getString("message"));
        n.setType(rs.getString("type"));
        n.setRead(rs.getInt("is_read") == 1);
        n.setCreatedAt(rs.getString("created_at"));
        return n;
    }

    public static List<Notification> findByUserEmail(String email) throws SQLException {
        PreparedStatement ps = Db.getConnection().prepareStatement("SELECT * FROM notifications WHERE user_email = ? ORDER BY created_at DESC");
        ps.setString(1, email);
        ResultSet rs = ps.executeQuery();
        List<Notification> list = new ArrayList<>();
        while (rs.next()) {
            list.add(mapResultSet(rs));
        }
        rs.close();
        ps.close();
        return list;
    }

    public static void save(Notification n) throws SQLException {
        PreparedStatement ps = Db.getConnection().prepareStatement(
                "INSERT INTO notifications (id,user_email,title,message,type,is_read,created_at) VALUES (?,?,?,?,?,?,?)");
        ps.setString(1, n.getId());
        ps.setString(2, n.getUserEmail());
        ps.setString(3, n.getTitle());
        ps.setString(4, n.getMessage());
        ps.setString(5, n.getType());
        ps.setInt(6, n.isRead() ? 1 : 0);
        ps.setString(7, n.getCreatedAt());
        ps.executeUpdate();
        ps.close();
    }

    public static void markAsRead(String email) throws SQLException {
        PreparedStatement ps = Db.getConnection().prepareStatement("UPDATE notifications SET is_read = 1 WHERE user_email = ?");
        ps.setString(1, email);
        ps.executeUpdate();
        ps.close();
    }
}

