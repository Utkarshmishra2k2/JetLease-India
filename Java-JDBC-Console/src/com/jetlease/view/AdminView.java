package com.jetlease.view;

import static com.jetlease.view.ConsoleUtil.*;

public class AdminView {

    public static void displayAdminMenu() {
        printHeader("Admin Console");
        System.out.println("1) Overview / Analytics");
        System.out.println("2) Aircraft");
        System.out.println("3) Bookings");
        System.out.println("4) Payments");
        System.out.println("5) Leases");
        System.out.println("6) Customers");
        System.out.println("7) Crew & Pilots");
        System.out.println("8) Routes");
        System.out.println("9) Inbox (Contact Messages & Issue Reports)");
        System.out.println("10) Export Reports (CSV)");
        System.out.println("11) Audit Log");
        System.out.println("0) Logout");
    }

    public static void displayOverviewHeader() {
        printHeader("Overview / Analytics");
    }
}