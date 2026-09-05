package com.jetlease.service;

import com.jetlease.dto.response.CostBreakdown;
import com.jetlease.dto.response.Recommendation;
import com.jetlease.entity.Aircraft;
import com.jetlease.entity.Route;
import com.jetlease.repository.AircraftRepository;
import com.jetlease.repository.RouteRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Ported 1:1 from CostCalculator.java. */
@Service
public class CostCalculatorService {

    private static final long PILOT_RATE_PER_HOUR = 45_000L;
    private static final long CREW_RATE_PER_HOUR = 12_000L;
    private static final int CREW_COUNT = 2;
    private static final long DOMESTIC_AIRPORT_CHARGES = 35_000L;
    private static final long HELICOPTER_AIRPORT_CHARGES = 18_000L;
    private static final double FUEL_SURCHARGE_RATE = 0.08;
    private static final double GST_RATE = 0.05;
    private static final double MARKUP = 1.2;

    private final RouteRepository routeRepository;
    private final AircraftRepository aircraftRepository;

    public CostCalculatorService(RouteRepository routeRepository, AircraftRepository aircraftRepository) {
        this.routeRepository = routeRepository;
        this.aircraftRepository = aircraftRepository;
    }

    public int routeDistanceKm(String originCode, String destCode) {
        Route o = routeRepository.findById(originCode).orElse(null);
        Route d = routeRepository.findById(destCode).orElse(null);
        if (o == null || d == null) return -1;
        return (int) Math.round(haversineKm(o.getLat(), o.getLon(), d.getLat(), d.getLon()));
    }

    public double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    /** Estimated block hours for a distance+speed, rounded up to the nearest quarter hour, minimum 1 hour. */
    public double estimateHours(int distanceKm, int speedKmh, boolean roundTrip) {
        double oneWayKm = roundTrip ? distanceKm * 2.0 : distanceKm;
        double hours = oneWayKm / (double) speedKmh;
        hours = Math.ceil(hours * 4) / 4.0;
        return Math.max(hours, 1.0);
    }

    public CostBreakdown calculateCost(long hourlyRate, String bookingType, String tripType, boolean selfFly,
                                        int distanceKm, int speedKmh) {
        boolean roundTrip = "Round Trip".equals(tripType);
        double hours = distanceKm > 0
                ? estimateHours(distanceKm, speedKmh, roundTrip)
                : (roundTrip ? 4.0 : 2.0); // fallback when route distance is unknown

        long aircraftCost = Math.round(hourlyRate * hours);
        long pilotCost = selfFly ? Math.round((PILOT_RATE_PER_HOUR * hours) / 2.0) : Math.round(PILOT_RATE_PER_HOUR * hours);
        long crewCost = Math.round(CREW_RATE_PER_HOUR * CREW_COUNT * hours);
        long airportCharges = "Helicopter Charter".equals(bookingType) ? HELICOPTER_AIRPORT_CHARGES : DOMESTIC_AIRPORT_CHARGES;

        long subtotal = aircraftCost + pilotCost + crewCost + airportCharges;
        long fuelSurcharge = Math.round(subtotal * FUEL_SURCHARGE_RATE);
        long gst = Math.round((subtotal + fuelSurcharge) * GST_RATE);
        long total = subtotal + fuelSurcharge + gst;

        CostBreakdown c = new CostBreakdown();
        c.hours = hours;
        c.aircraftCost = aircraftCost;
        c.pilotCost = pilotCost;
        c.crewCost = crewCost;
        c.airportCharges = airportCharges;
        c.fuelSurcharge = fuelSurcharge;
        c.gst = gst;
        c.total = total;
        return c;
    }

    public List<Recommendation> recommendAircraft(int pax, long budget, int distanceKm, String category) {
        List<Aircraft> candidates = aircraftRepository.findAll().stream()
                .filter(a -> "Available".equals(a.getStatus()))
                .filter(a -> a.getCapacity() >= pax)
                .filter(a -> a.getRangeKm() >= distanceKm)
                .filter(a -> category == null || category.equals(a.getCategory()))
                .toList();

        List<Recommendation> results = new ArrayList<>();
        for (Aircraft a : candidates) {
            double hours = estimateHours(distanceKm, a.getSpeed(), false);
            long estCost = Math.round(a.getHourlyRate() * hours * MARKUP);
            results.add(new Recommendation(a.getId(), a.getModel(), a.getCapacity(), a.getRangeKm(), estCost));
        }
        results.sort(Comparator.comparingLong(r -> r.estCost));
        return results;
    }

    public Recommendation bestWithinBudget(List<Recommendation> scored, long budget) {
        return scored.stream()
                .filter(r -> r.estCost <= budget)
                .findFirst()
                .orElse(scored.isEmpty() ? null : scored.get(0));
    }
}
