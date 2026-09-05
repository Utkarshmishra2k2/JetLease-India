package com.jetlease.view;

import static com.jetlease.view.ConsoleUtil.*;

public class LeaseView {

    public static void displayMyLeasesHeader() {
        printHeader("My Lease Agreements");
    }

    public static void renderLeaseText(String leaseId, String status, String contractText) {
        printHeader("Lease Agreement " + leaseId + "  [" + status + "]");
        System.out.println(contractText);
    }
}