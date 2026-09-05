package com.jetlease.view;

import java.util.List;
import static com.jetlease.view.ConsoleUtil.*;

import com.jetlease.model.entity.Aircraft;
import com.jetlease.model.entity.Route;
import com.jetlease.model.service.CostCalculator;

public class BookingView {

    public static void displayRoutes(List<Route> routes) {
        System.out.println("\nAvailable routes:");
        for (Route r : routes) {
            System.out.println("  " + r.getCode() + " - " + r.getCity());
        }
    }

    public static void renderAvailableAircraftList(List<Aircraft> fleet) {
        System.out.println("\nAvailable aircraft:");
        int n = 1;
        for (Aircraft a : fleet) {
            System.out.println("  " + n + ") " + a.getModel() + " - capacity " + a.getCapacity()
                    + " - " + fmtInr(a.getHourlyRate()) + "/hr");
            n++;
        }
    }

    public static void renderCostReview(Aircraft aircraft, String origin, String destination,
                                         String tripType, boolean isSelfFly, CostCalculator.Cost cost) {
        printHeader("Step 4 of 4: Cost Review");
        printLine("Aircraft", aircraft.getModel() + " (" + aircraft.getReg() + ")");
        printLine("Route", origin + " -> " + destination + (tripType.equals("Round Trip") ? " -> " + origin : ""));
        printLine("Estimated Block Hours", cost.hours);
        printLine(isSelfFly ? "Safety Pilot Cost" : "Pilot Cost", fmtInr(cost.pilotCost));
        printLine("Aircraft Cost", fmtInr(cost.aircraftCost));
        printLine("Crew Cost", fmtInr(cost.crewCost));
        printLine("Airport Charges", fmtInr(cost.airportCharges));
        printLine("Fuel Surcharge (8%)", fmtInr(cost.fuelSurcharge));
        printLine("Subtotal", fmtInr(cost.subtotal));
        printLine("GST (5%)", fmtInr(cost.gst));
        printLine("TOTAL PAYABLE", fmtInr(cost.total));
    }
}

