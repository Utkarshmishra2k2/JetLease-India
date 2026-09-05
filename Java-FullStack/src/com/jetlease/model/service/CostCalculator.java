package com.jetlease.model.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.jetlease.model.dao.Db;

public class CostCalculator {

    public static final int PILOT_RATE_PER_HOUR = 45000;
    public static final int CREW_RATE_PER_HOUR = 12000;
    public static final int AIRPORT_CHARGES_DOMESTIC = 35000;
    public static final int AIRPORT_CHARGES_HELICOPTER = 18000;
    public static final double FUEL_SURCHARGE_PCT = 0.08;
    public static final double GST_PCT = 0.05;
    public static final double SELF_FLY_SAFETY_PILOT_FACTOR = 0.5;

    public static class Cost {
        public double hours;
        public long aircraftCost, pilotCost, crewCost, airportCharges, fuelSurcharge, gst, subtotal, total;
    }

    public static double estimateBlockHours(String bookingType, String tripType) {
        double hrs = bookingType.equals("Helicopter Charter") ? 1 : 1.5;
        if (tripType.equals("Round Trip")) hrs *= 2;
        return hrs;
    }

    public static Cost calculateCost(long aircraftHourlyRate, String bookingType, String tripType, boolean selfFly) {
        Cost c = new Cost();
        c.hours = estimateBlockHours(bookingType, tripType);
        c.aircraftCost = Math.round(aircraftHourlyRate * c.hours);
        c.pilotCost = selfFly
                ? Math.round(PILOT_RATE_PER_HOUR * c.hours * SELF_FLY_SAFETY_PILOT_FACTOR)
                : Math.round(PILOT_RATE_PER_HOUR * c.hours);
        c.crewCost = Math.round(CREW_RATE_PER_HOUR * c.hours * 2);
        c.airportCharges = bookingType.equals("Helicopter Charter") ? AIRPORT_CHARGES_HELICOPTER : AIRPORT_CHARGES_DOMESTIC;
        c.fuelSurcharge = Math.round(c.aircraftCost * FUEL_SURCHARGE_PCT);
        c.subtotal = c.aircraftCost + c.pilotCost + c.crewCost + c.airportCharges + c.fuelSurcharge;
        c.gst = Math.round(c.subtotal * GST_PCT);
        c.total = c.subtotal + c.gst;
        return c;
    }

    public static int haversineKm(double lat1, double lon1, double lat2, double lon2) {
        double r = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.pow(Math.sin(dLat / 2), 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.pow(Math.sin(dLon / 2), 2);
        return (int) Math.round(r * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)));
    }

    public static int routeDistanceKm(String originCode, String destCode) throws SQLException {
        Connection conn = Db.getConnection();
        double[] o = fetchLatLng(conn, originCode);
        double[] d = fetchLatLng(conn, destCode);
        if (o == null || d == null || originCode.equals(destCode)) return -1;
        return haversineKm(o[0], o[1], d[0], d[1]);
    }

    private static double[] fetchLatLng(Connection conn, String code) throws SQLException {
        PreparedStatement ps = conn.prepareStatement("SELECT lat, lng FROM routes WHERE code = ?");
        ps.setString(1, code);
        ResultSet rs = ps.executeQuery();
        double[] result = null;
        if (rs.next()) result = new double[]{rs.getDouble("lat"), rs.getDouble("lng")};
        rs.close();
        ps.close();
        return result;
    }

    public static class Recommendation {
        public String id, model, category;
        public int capacity, range, speed;
        public long hourlyRate, estCost;
    }

    public static List<Recommendation> recommendAircraft(int passengers, long budgetInr, int distanceKm, String category) throws SQLException {
        Connection conn = Db.getConnection();
        PreparedStatement ps = conn.prepareStatement("SELECT * FROM aircraft WHERE status = 'Available'");
        ResultSet rs = ps.executeQuery();
        List<Recommendation> scored = new ArrayList<>();
        while (rs.next()) {
            String cat = rs.getString("category");
            boolean categoryOk = category != null ? cat.equals(category) : !cat.equals("Helicopter");
            if (!categoryOk) continue;
            int capacity = rs.getInt("capacity");
            int range = rs.getInt("range_km");
            if (capacity < passengers || range < distanceKm) continue;

            Recommendation r = new Recommendation();
            r.id = rs.getString("id");
            r.model = rs.getString("model");
            r.category = cat;
            r.capacity = capacity;
            r.range = range;
            r.speed = rs.getInt("speed");
            r.hourlyRate = rs.getLong("hourly_rate");
            double hours = (double) distanceKm / r.speed;
            r.estCost = Math.round(r.hourlyRate * hours * 1.2);
            scored.add(r);
        }
        rs.close();
        ps.close();
        scored.sort((a, b) -> Long.compare(a.estCost, b.estCost));
        return scored;
    }

    public static Recommendation bestWithinBudget(List<Recommendation> scored, long budgetInr) {
        Recommendation best = null;
        for (Recommendation r : scored) if (r.estCost <= budgetInr) best = r;
        if (best == null && !scored.isEmpty()) best = scored.get(0);
        return best;
    }
}

