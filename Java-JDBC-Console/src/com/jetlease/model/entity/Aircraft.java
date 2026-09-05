package com.jetlease.model.entity;

public class Aircraft {
    private String id;
    private String reg;
    private String model;
    private String manufacturer;
    private String category;
    private int capacity;
    private int speed;
    private int rangeKm;
    private long hourlyRate;
    private String status;
    private String typeRating;

    public Aircraft() {}

    public Aircraft(String id, String reg, String model, String manufacturer, String category,
                    int capacity, int speed, int rangeKm, long hourlyRate, String status, String typeRating) {
        this.id = id;
        this.reg = reg;
        this.model = model;
        this.manufacturer = manufacturer;
        this.category = category;
        this.capacity = capacity;
        this.speed = speed;
        this.rangeKm = rangeKm;
        this.hourlyRate = hourlyRate;
        this.status = status;
        this.typeRating = typeRating;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getReg() { return reg; }
    public void setReg(String reg) { this.reg = reg; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public int getSpeed() { return speed; }
    public void setSpeed(int speed) { this.speed = speed; }

    public int getRangeKm() { return rangeKm; }
    public void setRangeKm(int rangeKm) { this.rangeKm = rangeKm; }

    public long getHourlyRate() { return hourlyRate; }
    public void setHourlyRate(long hourlyRate) { this.hourlyRate = hourlyRate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getTypeRating() { return typeRating; }
    public void setTypeRating(String typeRating) { this.typeRating = typeRating; }
}
