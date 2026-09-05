package com.jetlease.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

public final class IdGen {
    private IdGen() {}

    private static final AtomicLong COUNTER = new AtomicLong(System.currentTimeMillis() % 100000);

    public static String uid(String prefix) {
        return prefix + "-" + COUNTER.incrementAndGet();
    }

    public static String nowIso() {
        return LocalDateTime.now().toString();
    }

    public static String todayIso() {
        return LocalDate.now().toString();
    }
}
