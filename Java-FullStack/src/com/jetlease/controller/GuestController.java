package com.jetlease.controller;

import java.sql.SQLException;

import static com.jetlease.view.ConsoleUtil.*;

import com.jetlease.model.dao.AircraftDao;
import com.jetlease.model.dao.ContactMessageDao;
import com.jetlease.model.dao.FaqDao;
import com.jetlease.model.dao.TestimonialDao;
import com.jetlease.model.entity.ContactMessage;
import com.jetlease.model.service.IdGen;
import com.jetlease.model.service.Validators;
import com.jetlease.view.GuestView;

public class GuestController {

    public static void run() throws SQLException {
        while (true) {
            GuestView.displayGuestMenu();
            int choice = readIntInRange("Choose: ", 0, 4);
            switch (choice) {
                case 1: browseFleet(); break;
                case 2: showFaq(); break;
                case 3: showTestimonials(); break;
                case 4: contactForm(); break;
                case 0: return;
            }
        }
    }

    private static void browseFleet() throws SQLException {
        GuestView.renderFleet(AircraftDao.findAll());
    }

    private static void showFaq() throws SQLException {
        GuestView.renderFaq(FaqDao.findAll());
    }

    private static void showTestimonials() throws SQLException {
        GuestView.renderTestimonials(TestimonialDao.findAll());
    }

    private static void contactForm() throws SQLException {
        printHeader("Contact Us");
        String name = readValidated("Name: ", Validators::name);
        String phone = readValidated("Phone (10 digits): ", Validators::phone10);
        String email = readValidated("Email: ", Validators::email);
        String message = readValidated("Message: ", Validators::message);

        ContactMessage msg = new ContactMessage();
        msg.setId(IdGen.uid("MSG"));
        msg.setName(name);
        msg.setPhone(phone);
        msg.setEmail(email);
        msg.setMessage(message);
        msg.setStatus("Unread");
        msg.setCreatedAt(IdGen.nowIso());

        ContactMessageDao.save(msg);

        System.out.println("\nThank you! Your message has been sent to our team.");
        pause();
    }
}
