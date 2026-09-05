package com.jetlease.model.entity;

public class Crew {

    private String id;
    private String name;
    private String role;

    private int dutyHours;
    private int remainingHours;

    private boolean available;

    public Crew() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public int getDutyHours() {
        return dutyHours;
    }

    public void setDutyHours(int dutyHours) {
        this.dutyHours = dutyHours;
    }

    public int getRemainingHours() {
        return remainingHours;
    }

    public void setRemainingHours(int remainingHours) {
        this.remainingHours = remainingHours;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}