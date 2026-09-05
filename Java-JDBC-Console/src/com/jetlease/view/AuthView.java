package com.jetlease.view;

import static com.jetlease.view.ConsoleUtil.*;

import com.jetlease.model.service.Validators;

public class AuthView {

    public static void displayRegisterHeader() {
        printHeader("Create your JetLease account");
    }

    public static void displayCustomerLoginMenu() {
        printHeader("Customer Login");
        System.out.println("1) Log in with Email + Password");
        System.out.println("2) Log in with Phone + Password");
        System.out.println("3) Log in with OTP");
        System.out.println("4) Forgot Password");
        System.out.println("0) Back");
    }

    public static void displayAdminLoginHeader() {
        printHeader("Admin Portal");
        System.out.println("Demo admin login: admin@jetlease.in / Admin@123");
    }

    public static String promptPasswordWithStrengthCheck(String promptLabel) {
        while (true) {
            String pw = readLine(promptLabel);
            int score = Validators.passwordScore(pw);
            System.out.println("  Strength: " + Validators.passwordLabel(score));
            if (score >= 3) return pw;
            System.out.println("  ! Password is too weak - use 8+ characters with upper, lower, number & symbol.");
        }
    }
}
