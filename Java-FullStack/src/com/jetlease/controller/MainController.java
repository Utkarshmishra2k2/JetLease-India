package com.jetlease.controller;

import java.sql.SQLException;

import static com.jetlease.view.ConsoleUtil.*;

import com.jetlease.view.MainView;

public class MainController {

    public void start() throws SQLException {
        MainView.displayHeader();
        mainMenu();
        MainView.displayGoodbye();
    }

    private void mainMenu() throws SQLException {
        while (true) {
            MainView.displayMainMenu();
            int choice = readIntInRange("Choose: ", 0, 4);

            switch (choice) {
                case 1:
                    GuestController.run();
                    break;
                case 2:
                    AuthController.register();
                    pause();
                    break;
                case 3: {
                    String email = AuthController.login();
                    if (email != null) CustomerController.run(email);
                    break;
                }
                case 4: {
                    String adminEmail = AuthController.adminLogin();
                    if (adminEmail != null) AdminController.run(adminEmail);
                    break;
                }
                case 0:
                    return;
            }
        }
    }
}
