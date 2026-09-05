package com.jetlease.model.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.jetlease.model.entity.Payment;

public class PaymentDao {

    private static Payment mapResultSet(ResultSet rs) throws SQLException {
        Payment p = new Payment();
        p.setId(rs.getString("id"));
        p.setBookingId(rs.getString("booking_id"));
        p.setUserEmail(rs.getString("user_email"));
        p.setAmount(rs.getLong("amount"));
        p.setTransactionId(rs.getString("transaction_id"));
        p.setStatus(rs.getString("status"));
        p.setSubmittedAt(rs.getString("submitted_at"));
        p.setCancellationFee(rs.getLong("cancellation_fee"));
        p.setRefundAmount(rs.getLong("refund_amount"));
        return p;
    }

    public static List<Payment> findAll() throws SQLException {
        PreparedStatement ps = Db.getConnection().prepareStatement("SELECT * FROM payments ORDER BY submitted_at DESC");
        ResultSet rs = ps.executeQuery();
        List<Payment> list = new ArrayList<>();
        while (rs.next()) {
            list.add(mapResultSet(rs));
        }
        rs.close();
        ps.close();
        return list;
    }

    public static Payment findById(String id) throws SQLException {
        PreparedStatement ps = Db.getConnection().prepareStatement("SELECT * FROM payments WHERE id = ?");
        ps.setString(1, id);
        ResultSet rs = ps.executeQuery();
        Payment p = null;
        if (rs.next()) {
            p = mapResultSet(rs);
        }
        rs.close();
        ps.close();
        return p;
    }

    public static List<Payment> findByUserEmail(String email) throws SQLException {
        PreparedStatement ps = Db.getConnection().prepareStatement("SELECT * FROM payments WHERE user_email = ? ORDER BY submitted_at DESC");
        ps.setString(1, email);
        ResultSet rs = ps.executeQuery();
        List<Payment> list = new ArrayList<>();
        while (rs.next()) {
            list.add(mapResultSet(rs));
        }
        rs.close();
        ps.close();
        return list;
    }

    public static Payment findByBookingId(String bookingId) throws SQLException {
        PreparedStatement ps = Db.getConnection().prepareStatement("SELECT * FROM payments WHERE booking_id = ?");
        ps.setString(1, bookingId);
        ResultSet rs = ps.executeQuery();
        Payment p = null;
        if (rs.next()) {
            p = mapResultSet(rs);
        }
        rs.close();
        ps.close();
        return p;
    }

    public static void save(Payment p) throws SQLException {
        PreparedStatement ps = Db.getConnection().prepareStatement(
                "INSERT INTO payments (id,booking_id,user_email,amount,transaction_id,status,submitted_at,cancellation_fee,refund_amount) " +
                "VALUES (?,?,?,?,?,?,?,?,?)");
        ps.setString(1, p.getId());
        ps.setString(2, p.getBookingId());
        ps.setString(3, p.getUserEmail());
        ps.setLong(4, p.getAmount());
        ps.setString(5, p.getTransactionId());
        ps.setString(6, p.getStatus());
        ps.setString(7, p.getSubmittedAt());
        ps.setLong(8, p.getCancellationFee());
        ps.setLong(9, p.getRefundAmount());
        ps.executeUpdate();
        ps.close();
    }

    public static void updateStatus(String id, String status) throws SQLException {
        PreparedStatement ps = Db.getConnection().prepareStatement("UPDATE payments SET status = ? WHERE id = ?");
        ps.setString(1, status);
        ps.setString(2, id);
        ps.executeUpdate();
        ps.close();
    }

    public static void updateRefund(String id, long cancellationFee, long refundAmount, String status) throws SQLException {
        PreparedStatement ps = Db.getConnection().prepareStatement("UPDATE payments SET cancellation_fee = ?, refund_amount = ?, status = ? WHERE id = ?");
        ps.setLong(1, cancellationFee);
        ps.setLong(2, refundAmount);
        ps.setString(3, status);
        ps.setString(4, id);
        ps.executeUpdate();
        ps.close();
    }
}
