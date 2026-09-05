package com.jetlease.controller;

import com.jetlease.dto.request.RecommendRequest;
import com.jetlease.dto.response.Recommendation;
import com.jetlease.dto.response.RecommendationResult;
import com.jetlease.entity.Aircraft;
import com.jetlease.entity.Route;
import com.jetlease.repository.AircraftRepository;
import com.jetlease.repository.RouteRepository;
import com.jetlease.service.CostCalculatorService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** Aircraft catalog, routes and distance/recommendation lookups used by the booking wizard. */
@RestController
@RequestMapping("/api")
public class CatalogController {

    private final AircraftRepository aircraftRepository;
    private final RouteRepository routeRepository;
    private final CostCalculatorService costCalculatorService;

    public CatalogController(AircraftRepository aircraftRepository, RouteRepository routeRepository,
                              CostCalculatorService costCalculatorService) {
        this.aircraftRepository = aircraftRepository;
        this.routeRepository = routeRepository;
        this.costCalculatorService = costCalculatorService;
    }

    @GetMapping("/routes")
    public List<Route> routes() {
        return routeRepository.findAll();
    }

    @GetMapping("/routes/distance")
    public Map<String, Object> distance(@RequestParam String origin, @RequestParam String destination) {
        int km = costCalculatorService.routeDistanceKm(origin, destination);
        return Map.of("distanceKm", km);
    }

    @GetMapping("/aircraft")
    public List<Aircraft> aircraft() {
        return aircraftRepository.findAll();
    }

    @GetMapping("/aircraft/available")
    public List<Aircraft> availableAircraft(@RequestParam(required = false) String category,
                                             @RequestParam int pax) {
        if (category != null && !category.isBlank()) {
            return aircraftRepository.findByStatusAndCategoryAndCapacityGreaterThanEqual("Available", category, pax);
        }
        return aircraftRepository.findByStatusAndCapacityGreaterThanEqual("Available", pax);
    }

    @PostMapping("/aircraft/recommend")
    public RecommendationResult recommend(@RequestBody RecommendRequest req) {
        List<Recommendation> scored = costCalculatorService.recommendAircraft(
                req.getPax(), req.getBudget(), req.getDistanceKm(), req.getCategory());
        if (scored.isEmpty()) return new RecommendationResult(null, List.of());
        Recommendation best = costCalculatorService.bestWithinBudget(scored, req.getBudget());
        List<Recommendation> others = scored.stream().filter(r -> !r.id.equals(best.id)).limit(3).toList();
        return new RecommendationResult(best, others);
    }
}
