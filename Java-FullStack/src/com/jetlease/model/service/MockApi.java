package com.jetlease.model.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.jetlease.model.dao.Db;
import com.jetlease.view.ConsoleUtil;

public class MockApi {

    public static final String MOCK_OTP_CODE = "123456";

    public static boolean verifyOtp(String code) {
        return MOCK_OTP_CODE.equals(code == null ? "" : code.trim());
    }

    private static void simulateNetworkDelay(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) { }
    }

    public static class VerifyResult {
        public boolean verified;
        public String message;
        public String holderName, dob, gender, licenseClass;
        public int hoursOnRecord;
    }

    public static VerifyResult verifyAadhaar(String aadhaarNumber) throws SQLException {
        System.out.println("  ...verifying with the Aadhaar registry...");
        simulateNetworkDelay(500);
        Connection conn = Db.getConnection();
        PreparedStatement ps = conn.prepareStatement("SELECT * FROM aadhaar_registry WHERE aadhaar_number = ?");
        ps.setString(1, aadhaarNumber.trim());
        ResultSet rs = ps.executeQuery();
        VerifyResult result = new VerifyResult();
        if (!rs.next()) {
            result.verified = false;
            result.message = "Aadhaar number not found in the registry.";
        } else {
            String status = rs.getString("status");
            if (!status.equals("Active")) {
                result.verified = false;
                result.message = "Aadhaar found but its status is \"" + status + "\", not Active.";
            } else {
                result.verified = true;
                result.holderName = rs.getString("holder_name");
                result.dob = rs.getString("dob");
                result.gender = rs.getString("gender");
                result.message = "Aadhaar verified - registered to " + result.holderName + ".";
            }
        }
        rs.close();
        ps.close();
        return result;
    }

    public static VerifyResult verifyPilotLicense(String licenseNumber) throws SQLException {
        System.out.println("  ...verifying with the DGCA registry...");
        simulateNetworkDelay(500);
        Connection conn = Db.getConnection();
        PreparedStatement ps = conn.prepareStatement("SELECT * FROM pilot_license_registry WHERE LOWER(license_number) = LOWER(?)");
        ps.setString(1, licenseNumber.trim());
        ResultSet rs = ps.executeQuery();
        VerifyResult result = new VerifyResult();
        if (!rs.next()) {
            result.verified = false;
            result.message = "License number not found in the DGCA registry.";
        } else {
            String status = rs.getString("status");
            if (!status.equals("Active")) {
                result.verified = false;
                result.message = "License found but its status is \"" + status + "\", not Active.";
            } else {
                result.verified = true;
                result.holderName = rs.getString("holder_name");
                result.licenseClass = rs.getString("license_class");
                result.hoursOnRecord = rs.getInt("hours_on_record");
                result.message = "License verified - registered to " + result.holderName + ", " + result.hoursOnRecord + " hours on record.";
            }
        }
        rs.close();
        ps.close();
        return result;
    }

    public static void recordLedgerEntry(String transactionId, String bookingId, long amount) throws SQLException {
        PreparedStatement ps = Db.getConnection().prepareStatement(
                "INSERT INTO bank_ledger (transaction_id, booking_id, amount, status, cleared_at) VALUES (?,?,?,?,?)");
        ps.setString(1, transactionId);
        ps.setString(2, bookingId);
        ps.setLong(3, amount);
        ps.setString(4, "CLEARED");
        ps.setString(5, IdGen.nowIso());
        ps.executeUpdate();
        ps.close();
    }

    public static VerifyResult verifyPaymentAgainstLedger(String transactionId, String bookingId, long amount) throws SQLException {
        System.out.println("  ...checking the bank ledger...");
        simulateNetworkDelay(600);
        Connection conn = Db.getConnection();
        PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM bank_ledger WHERE transaction_id = ? AND booking_id = ?");
        ps.setString(1, transactionId);
        ps.setString(2, bookingId);
        ResultSet rs = ps.executeQuery();
        VerifyResult result = new VerifyResult();
        if (!rs.next()) {
            result.verified = false;
            result.message = "Transaction ID not found in the bank ledger.";
        } else {
            long ledgerAmount = rs.getLong("amount");
            if (ledgerAmount != amount) {
                result.verified = false;
                result.message = "Transaction found, but the settled amount does not match the invoice.";
            } else {
                result.verified = true;
                result.message = "Bank ledger confirms this transaction cleared for " + ConsoleUtil.fmtInr(ledgerAmount)
                        + " on " + rs.getString("cleared_at") + ".";
            }
        }
        rs.close();
        ps.close();
        return result;
    }
}
