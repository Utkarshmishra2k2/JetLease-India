package com.jetlease.model.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.jetlease.model.entity.User;

public class UserDao {

    private static User mapResultSet(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getString("id"));
        user.setFullName(rs.getString("full_name"));
        user.setEmail(rs.getString("email"));
        user.setPhone(rs.getString("phone"));
        user.setDob(rs.getString("dob"));
        user.setEmergencyContact(rs.getString("emergency_contact"));
        user.setPassword(rs.getString("password"));
        user.setCountry(rs.getString("country"));
        user.setRole(rs.getString("role"));
        user.setStatus(rs.getString("status"));
        user.setMembership(rs.getString("membership"));
        user.setLoyaltyPoints(rs.getInt("loyalty_points"));
        user.setCreatedAt(rs.getString("created_at"));
        return user;
    }

    public static User findByEmail(String email) throws SQLException {
        PreparedStatement ps = Db.getConnection().prepareStatement("SELECT * FROM users WHERE email = ?");
        ps.setString(1, email);
        ResultSet rs = ps.executeQuery();
        User user = null;
        if (rs.next()) {
            user = mapResultSet(rs);
        }
        rs.close();
        ps.close();
        return user;
    }

    public static User findById(String id) throws SQLException {
        PreparedStatement ps = Db.getConnection().prepareStatement("SELECT * FROM users WHERE id = ?");
        ps.setString(1, id);
        ResultSet rs = ps.executeQuery();
        User user = null;
        if (rs.next()) {
            user = mapResultSet(rs);
        }
        rs.close();
        ps.close();
        return user;
    }

    public static boolean emailExists(String email) throws SQLException {
        return findByEmail(email) != null;
    }

    public static void save(User user) throws SQLException {
        PreparedStatement ps = Db.getConnection().prepareStatement(
                "INSERT INTO users (id,full_name,email,phone,dob,emergency_contact,password,country,role,status,membership,loyalty_points,created_at) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)");
        ps.setString(1, user.getId());
        ps.setString(2, user.getFullName());
        ps.setString(3, user.getEmail());
        ps.setString(4, user.getPhone());
        ps.setString(5, user.getDob());
        ps.setString(6, user.getEmergencyContact());
        ps.setString(7, user.getPassword());
        ps.setString(8, user.getCountry());
        ps.setString(9, user.getRole());
        ps.setString(10, user.getStatus());
        ps.setString(11, user.getMembership());
        ps.setInt(12, user.getLoyaltyPoints());
        ps.setString(13, user.getCreatedAt());
        ps.executeUpdate();
        ps.close();
    }

    public static void updateUserField(String email, String field, String val) throws SQLException {
        PreparedStatement ps = Db.getConnection().prepareStatement("UPDATE users SET " + field + " = ? WHERE email = ?");
        ps.setString(1, val);
        ps.setString(2, email);
        ps.executeUpdate();
        ps.close();
    }

    public static void updateLoyaltyPoints(String email, int points) throws SQLException {
        PreparedStatement ps = Db.getConnection().prepareStatement("UPDATE users SET loyalty_points = ? WHERE email = ?");
        ps.setInt(1, points);
        ps.setString(2, email);
        ps.executeUpdate();
        ps.close();
    }

    public static List<User> findAllCustomers() throws SQLException {
        PreparedStatement ps = Db.getConnection().prepareStatement("SELECT * FROM users WHERE role = 'customer' ORDER BY full_name");
        ResultSet rs = ps.executeQuery();
        List<User> list = new ArrayList<>();
        while (rs.next()) {
            list.add(mapResultSet(rs));
        }
        rs.close();
        ps.close();
        return list;
    }
}
