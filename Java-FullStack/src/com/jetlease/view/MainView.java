package com.jetlease.view;

import static com.jetlease.view.ConsoleUtil.*;

public class MainView {

    public static void displayHeader() {
        System.out.println("======================================================================");
        System.out.println("   JETLEASE INDIA - Private Jet & Helicopter Charter Console");
        System.out.println("======================================================================");
        System.out.println("Demo customer login: demo@jetlease.in / Demo@123");
        System.out.println("Demo admin login:    admin@jetlease.in / Admin@123");
    }

    public static void displayMainMenu() {
        printHeader("Main Menu");
        System.out.println("1) Browse as Guest (Fleet, FAQ, Testimonials, Contact Us)");
        System.out.println("2) Register New Account");
        System.out.println("3) Customer Login");
        System.out.println("4) Admin Portal");
        System.out.println("0) Exit");
    }

    public static void displayGoodbye() {
        System.out.println("\nThank you for using JetLease India. Goodbye!");
    }
}
