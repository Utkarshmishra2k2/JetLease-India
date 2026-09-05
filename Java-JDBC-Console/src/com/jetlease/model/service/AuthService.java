package com.jetlease.model.service;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.jetlease.model.dao.Db;
import com.jetlease.model.dao.UserDao;
import com.jetlease.model.entity.User;

public class AuthService {

    public static boolean emailExists(String email) throws SQLException {
        return UserDao.emailExists(email);
    }

    public static User registerUser(String fullName, String email, String phone, String dob, String emergencyContact, String password) throws SQLException {
        String id = IdGen.uid("CUS");
        User user = new User(id, fullName, email.toLowerCase(), phone, dob, emergencyContact,
                password, "India", "customer", "active", "none", 0, IdGen.nowIso());
        UserDao.save(user);

        BookingRules.addAudit(email, "Login", "Account Registered", "Self-registered via console");
        BookingRules.addNotification(email, "Welcome to JetLease",
                "Your account has been created. Explore the fleet and book your first flight.", "success");
        return user;
    }

    public static String findCustomerByCredential(String field, String value, String password) throws SQLException {
        PreparedStatement ps = Db.getConnection().prepareStatement(
                "SELECT email FROM users WHERE " + field + " = ? AND password = ? AND role = 'customer'");
        ps.setString(1, value);
        ps.setString(2, password);
        ResultSet rs = ps.executeQuery();
        String email = rs.next() ? rs.getString("email") : null;
        rs.close();
        ps.close();
        return email;
    }

    public static String findCustomerByIdentifier(String idOrPhone) throws SQLException {
        PreparedStatement ps = Db.getConnection().prepareStatement(
                "SELECT email FROM users WHERE (email = ? OR phone = ?) AND role = 'customer'");
        ps.setString(1, idOrPhone.toLowerCase());
        ps.setString(2, idOrPhone);
        ResultSet rs = ps.executeQuery();
        String email = rs.next() ? rs.getString("email") : null;
        rs.close();
        ps.close();
        return email;
    }

    public static String authenticateAdmin(String email, String password) throws SQLException {
        PreparedStatement ps = Db.getConnection().prepareStatement(
                "SELECT email FROM users WHERE email = ? AND password = ? AND role = 'admin'");
        ps.setString(1, email.toLowerCase());
        ps.setString(2, password);
        ResultSet rs = ps.executeQuery();
        boolean ok = rs.next();
        rs.close();
        ps.close();
        if (!ok) return null;
        BookingRules.addAudit(email, "Login", "Admin Login", "");
        return email;
    }

    public static String getUserField(String email, String field) throws SQLException {
        User user = UserDao.findByEmail(email);
        if (user == null) return null;
        switch (field) {
            case "full_name": return user.getFullName();
            case "email": return user.getEmail();
            case "phone": return user.getPhone();
            case "dob": return user.getDob();
            case "emergency_contact": return user.getEmergencyContact();
            case "password": return user.getPassword();
            case "country": return user.getCountry();
            case "role": return user.getRole();
            case "status": return user.getStatus();
            case "membership": return user.getMembership();
            case "loyalty_points": return String.valueOf(user.getLoyaltyPoints());
            case "created_at": return user.getCreatedAt();
            default: return null;
        }
    }

    public static void updatePassword(String email, String newPassword) throws SQLException {
        UserDao.updateUserField(email, "password", newPassword);
        BookingRules.addAudit(email, "Login", "Password Reset via Dashboard", "");
    }
}
