package com.jetlease.view;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Scanner;
import java.util.function.Function;

public class ConsoleUtil {

    public static final Scanner sc = new Scanner(System.in);

    public static String readLine(String prompt) {
        System.out.print(prompt);
        return sc.nextLine().trim();
    }

    public static String readValidated(String prompt, Function<String, String> validator) {
        while (true) {
            String value = readLine(prompt);
            String error = validator.apply(value);
            if (error.isEmpty()) return value;
            System.out.println("  ! " + error);
        }
    }

    public static int readInt(String prompt) {
        while (true) {
            String v = readLine(prompt);
            try {
                return Integer.parseInt(v.trim());
            } catch (NumberFormatException e) {
                System.out.println("  ! Please enter a whole number.");
            }
        }
    }

    public static int readIntInRange(String prompt, int min, int max) {
        while (true) {
            int v = readInt(prompt);
            if (v < min || v > max) {
                System.out.println("  ! Please enter a number between " + min + " and " + max + ".");
                continue;
            }
            return v;
        }
    }

    public static double readDouble(String prompt) {
        while (true) {
            String v = readLine(prompt);
            try {
                return Double.parseDouble(v.trim());
            } catch (NumberFormatException e) {
                System.out.println("  ! Please enter a valid number.");
            }
        }
    }

    public static String readDate(String prompt) {
        while (true) {
            String v = readLine(prompt + " (yyyy-MM-dd): ");
            try {
                LocalDate.parse(v);
                return v;
            } catch (DateTimeParseException e) {
                System.out.println("  ! Please enter a valid date in yyyy-MM-dd format.");
            }
        }
    }

    public static boolean readYesNo(String prompt) {
        while (true) {
            String v = readLine(prompt + " (y/n): ").toLowerCase(Locale.ROOT);
            if (v.equals("y") || v.equals("yes")) return true;
            if (v.equals("n") || v.equals("no")) return false;
            System.out.println("  ! Please answer y or n.");
        }
    }

    public static void pause() {
        System.out.print("\nPress Enter to continue...");
        sc.nextLine();
    }

    public static void printDivider() {
        System.out.println("--------------------------------------------------------------------");
    }

    public static void printHeader(String title) {
        printDivider();
        System.out.println(" " + title);
        printDivider();
    }

    public static void printLine(String label, Object value) {
        System.out.printf("  %-28s %s%n", label + ":", value);
    }

    public static String fmtInr(long amount) {
        boolean negative = amount < 0;
        String digits = Long.toString(Math.abs(amount));
        StringBuilder out = new StringBuilder();
        int len = digits.length();
        if (len <= 3) {
            out.append(digits);
        } else {
            out.append(digits.substring(len - 3));
            String rest = digits.substring(0, len - 3);
            while (rest.length() > 2) {
                out.insert(0, rest.substring(rest.length() - 2) + ",");
                rest = rest.substring(0, rest.length() - 2);
            }
            if (!rest.isEmpty()) out.insert(0, rest + ",");
        }
        return (negative ? "-" : "") + "\u20B9" + out;
    }
}

