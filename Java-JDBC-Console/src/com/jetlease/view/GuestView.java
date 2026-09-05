package com.jetlease.view;

import java.util.List;
import static com.jetlease.view.ConsoleUtil.*;

import com.jetlease.model.entity.Aircraft;
import com.jetlease.model.entity.Faq;
import com.jetlease.model.entity.Testimonial;

public class GuestView {

    public static void displayGuestMenu() {
        printHeader("JetLease India - Welcome");
        System.out.println("1) Browse Our Fleet");
        System.out.println("2) Frequently Asked Questions");
        System.out.println("3) Customer Testimonials");
        System.out.println("4) Contact Us");
        System.out.println("0) Back");
    }

    public static void renderFleet(List<Aircraft> fleet) {
        printHeader("Our Fleet");
        for (Aircraft a : fleet) {
            System.out.println();
            System.out.println(a.getModel() + " (" + a.getCategory() + ") - " + a.getStatus());
            printLine("Manufacturer", a.getManufacturer());
            printLine("Capacity", a.getCapacity() + " passengers");
            printLine("Range", a.getRangeKm() + " km");
            printLine("Speed", a.getSpeed() + " km/h");
            printLine("Hourly Rate", fmtInr(a.getHourlyRate()));
        }
        pause();
    }

    public static void renderFaq(List<Faq> faqs) {
        printHeader("Frequently Asked Questions");
        int n = 1;
        for (Faq f : faqs) {
            System.out.println("\nQ" + n + ". " + f.getQuestion());
            System.out.println("A" + n + ". " + f.getAnswer());
            n++;
        }
        pause();
    }

    public static void renderTestimonials(List<Testimonial> testimonials) {
        printHeader("What Our Clients Say");
        for (Testimonial t : testimonials) {
            System.out.println("\n\"" + t.getQuote() + "\"");
            System.out.println("  - " + t.getName() + ", " + t.getRole());
        }
        pause();
    }
}
