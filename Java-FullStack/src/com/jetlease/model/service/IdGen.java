package com.jetlease.model.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class IdGen {

    private static final Random RANDOM = new Random();

    public static String uid(String prefix) {
        String time = Long.toString(System.currentTimeMillis(), 36).toUpperCase();
        int rand = 100 + RANDOM.nextInt(900);
        return prefix + "-" + time + "-" + rand;
    }

    public static String nowIso() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public static String todayIso() {
        return LocalDate.now().toString();
    }
}