package com.jetlease.dto.response;

import java.util.List;

public class RecommendationResult {
    public Recommendation best;
    public List<Recommendation> alternatives;

    public RecommendationResult(Recommendation best, List<Recommendation> alternatives) {
        this.best = best;
        this.alternatives = alternatives;
    }
}
