package com.jetlease.model.entity;

public class Pilot {
    private String id;
    private String name;
    private String licenseNumber;
    private int flyingHours;
    private int remainingHours;
    private String typeRatings;
    private String certifications;
    private boolean available;

    public Pilot() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }

    public int getFlyingHours() { return flyingHours; }
    public void setFlyingHours(int flyingHours) { this.flyingHours = flyingHours; }

    public int getRemainingHours() { return remainingHours; }
    public void setRemainingHours(int remainingHours) { this.remainingHours = remainingHours; }

    public String getTypeRatings() { return typeRatings; }
    public void setTypeRatings(String typeRatings) { this.typeRatings = typeRatings; }

    public String getCertifications() { return certifications; }
    public void setCertifications(String certifications) { this.certifications = certifications; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
}
