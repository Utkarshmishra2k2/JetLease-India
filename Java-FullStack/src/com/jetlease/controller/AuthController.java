package com.jetlease.controller;

import java.sql.SQLException;

import static com.jetlease.view.ConsoleUtil.*;

import com.jetlease.model.service.AuthService;
import com.jetlease.model.service.BookingRules;
import com.jetlease.model.service.MockApi;
import com.jetlease.model.service.Validators;
import com.jetlease.view.AuthView;

public class AuthController {

    public static void register() throws SQLException {
        AuthView.displayRegisterHeader();

        String fullName = readValidated("Full Name: ", Validators::name);
        String email = readValidated("Email: ", Validators::email).toLowerCase();

        if (AuthService.emailExists(email)) {
            System.out.println("  ! An account with this email already exists.");
            return;
        }

        String phone = readValidated("Phone (10 digits): ", Validators::phone10);

        String dob = readValidated("Date of Birth (yyyy-MM-dd): ", v -> {
            String err = Validators.dob(v);
            if (err.isEmpty() && !Validators.isAdult(v)) return "You must be 18 or older.";
            return err;
        });

        String emergencyContact = readValidated("Emergency Contact (10 digits): ", Validators::phone10);

        String password = AuthView.promptPasswordWithStrengthCheck("Password: ");
        String confirm = readLine("Confirm Password: ");
        if (!password.equals(confirm)) {
            System.out.println("  ! Passwords do not match. Registration cancelled.");
            return;
        }

        System.out.println("\nMock OTPs sent to email & phone. Enter 123456 for both (demo mode).");
        String emailOtp = readLine("Email OTP: ");
        String phoneOtp = readLine("Phone OTP: ");
        if (!MockApi.verifyOtp(emailOtp) || !MockApi.verifyOtp(phoneOtp)) {
            System.out.println("  ! Incorrect OTP. Use 123456 for this demo. Registration cancelled.");
            return;
        }

        AuthService.registerUser(fullName, email, phone, dob, emergencyContact, password);
        System.out.println("\nAccount created! You can now log in.");
    }

    public static String login() throws SQLException {
        AuthView.displayCustomerLoginMenu();
        int choice = readIntInRange("Choose: ", 0, 4);

        String email;
        switch (choice) {
            case 1: {
                String e = readLine("Email: ").toLowerCase();
                String pw = readLine("Password: ");
                email = AuthService.findCustomerByCredential("email", e, pw);
                break;
            }
            case 2: {
                String p = readLine("Phone: ");
                String pw = readLine("Password: ");
                email = AuthService.findCustomerByCredential("phone", p, pw);
                break;
            }
            case 3: {
                String id = readLine("Email or Phone: ");
                System.out.println("OTP sent (demo code: 123456).");
                String otp = readLine("Enter OTP: ");
                if (!MockApi.verifyOtp(otp)) {
                    System.out.println("  ! Incorrect OTP. Use 123456 for this demo.");
                    return null;
                }
                email = AuthService.findCustomerByIdentifier(id);
                break;
            }
            case 4:
                forgotPasswordQuick();
                return null;
            default:
                return null;
        }

        if (email == null) {
            System.out.println("  ! Invalid credentials. Try demo@jetlease.in / Demo@123");
            return null;
        }

        String status = AuthService.getUserField(email, "status");
        if ("suspended".equals(status)) {
            System.out.println("  ! This account has been suspended. Contact support.");
            return null;
        }

        BookingRules.addAudit(email, "Login", "Customer Login", "");
        String fullName = AuthService.getUserField(email, "full_name");
        System.out.println("\nWelcome back, " + (fullName != null ? fullName.split(" ")[0] : "") + "!");
        return email;
    }

    public static String adminLogin() throws SQLException {
        AuthView.displayAdminLoginHeader();
        String email = readLine("Admin Email: ").toLowerCase();
        String pw = readLine("Password: ");

        String authenticatedAdmin = AuthService.authenticateAdmin(email, pw);
        if (authenticatedAdmin == null) {
            System.out.println("  ! Invalid admin credentials.");
            return null;
        }
        System.out.println("\nWelcome back, Admin.");
        return authenticatedAdmin;
    }

    private static void forgotPasswordQuick() throws SQLException {
        String email = readLine("Enter your account email: ").toLowerCase();
        if (!AuthService.emailExists(email)) {
            System.out.println("  ! No account found with that email.");
            return;
        }
        System.out.println("Password reset link sent (demo). Check your inbox.");
    }

    public static void forgotPasswordFull(String email) throws SQLException {
        System.out.println("\nSend a mock OTP to your registered email to reset your password.");
        if (!readYesNo("Send OTP now?")) return;
        System.out.println("Mock OTP sent to " + email + " (use 123456).");
        String otp = readLine("Enter OTP: ");
        if (!MockApi.verifyOtp(otp)) {
            System.out.println("  ! Incorrect OTP. Use 123456 for this demo.");
            return;
        }
        String newPw = AuthView.promptPasswordWithStrengthCheck("New Password: ");
        String confirm = readLine("Confirm New Password: ");
        if (!newPw.equals(confirm)) {
            System.out.println("  ! Passwords do not match.");
            return;
        }
        AuthService.updatePassword(email, newPw);
        System.out.println("Password updated successfully.");
    }
}
