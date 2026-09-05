package com.jetlease;

import com.jetlease.controller.MainController;
import java.sql.SQLException;

public class Main {

    public static void main(String[] args) {

        MainController controller = new MainController();

        try {
            controller.start();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}