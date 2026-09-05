package com.jetlease.util;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeParseException;

/** Ported 1:1 from the original console app's Validators.java business rules. */
public final class Validators {
    private Validators() {}

    public static String name(String v) {
        if (v == null || v.trim().isEmpty()) return "This field is required.";
        String val = v.trim();
        if (!val.matches("^[A-Za-z][A-Za-z\\s.'-]{1,49}$")) return "Only letters are allowed.";
        return "";
    }

    public static String email(String v) {
        if (v == null || v.trim().isEmpty()) return "Email is required.";
        if (!v.trim().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) return "Enter a valid email address.";
        return "";
    }

    public static String phone10(String v) {
        if (v == null || v.trim().isEmpty()) return "Phone number is required.";
        if (!v.trim().matches("^[0-9]{10}$")) return "Enter a valid 10-digit phone number (numbers only).";
        return "";
    }

    public static String aadhaar(String v) {
        if (v == null || v.trim().isEmpty()) return "Aadhaar number is required.";
        if (!v.trim().matches("^[0-9]{12}$")) return "Aadhaar number must be exactly 12 digits.";
        return "";
    }

    public static String licenseNumber(String v) {
        if (v == null || v.trim().isEmpty()) return "License number is required.";
        if (!v.trim().matches("^[A-Za-z0-9-]{4,20}$")) return "Enter a valid license number (letters, numbers, hyphens only).";
        return "";
    }

    public static String message(String v) {
        if (v == null || v.trim().isEmpty()) return "Message is required.";
        if (v.trim().length() < 10) return "Message must be at least 10 characters.";
        return "";
    }

    public static String dob(String v) {
        if (v == null || v.isEmpty()) return "Date of birth is required.";
        LocalDate dob;
        try {
            dob = LocalDate.parse(v);
        } catch (DateTimeParseException e) {
            return "Enter a valid date (yyyy-MM-dd).";
        }
        LocalDate today = LocalDate.now();
        if (dob.isBefore(today.minusYears(100))) return "Age cannot be more than 100 years.";
        if (dob.isAfter(today.minusDays(15))) return "Passenger must be at least 15 days old - future dates are not allowed.";
        return "";
    }

    public static boolean isAdult(String dobStr) {
        try {
            LocalDate dob = LocalDate.parse(dobStr);
            if (dob.isAfter(LocalDate.now())) return false;
            return Period.between(dob, LocalDate.now()).getYears() >= 18;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isAadhaarExempt(String dobStr) {
        try {
            LocalDate dob = LocalDate.parse(dobStr);
            LocalDate threshold = dob.plusYears(5).plusDays(15);
            return LocalDate.now().isBefore(threshold);
        } catch (Exception e) {
            return false;
        }
    }

    public static int passwordScore(String pw) {
        int score = 0;
        if (pw.length() >= 8) score++;
        if (pw.matches(".*[A-Z].*")) score++;
        if (pw.matches(".*[a-z].*")) score++;
        if (pw.matches(".*[0-9].*")) score++;
        if (pw.matches(".*[^A-Za-z0-9].*")) score++;
        return score;
    }

    public static String passwordLabel(int score) {
        String[] levels = {"Very Weak", "Weak", "Fair", "Good", "Strong", "Very Strong"};
        return levels[Math.max(0, Math.min(score, levels.length - 1))];
    }
}
