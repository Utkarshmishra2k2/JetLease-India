package com.jetlease.dto.response;

public class Recommendation {
    public String id;
    public String model;
    public int capacity;
    public int range;
    public long estCost;

    public Recommendation(String id, String model, int capacity, int range, long estCost) {
        this.id = id;
        this.model = model;
        this.capacity = capacity;
        this.range = range;
        this.estCost = estCost;
    }
}
