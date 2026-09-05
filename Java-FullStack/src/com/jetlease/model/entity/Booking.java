package com.jetlease.model.entity;

public class Booking {
    private String id;
    private String userEmail;
    private String type;
    private String tripType;
    private String origin;
    private String destination;
    private String date;
    private String time;
    private String returnDate;
    private String returnTime;
    private int pax;
    private String aircraftId;
    private String aircraftModel;
    private boolean selfFly;
    private String licenseNumber;
    private String licenseClass;
    private int flyingHours;
    private String certificateFileName;
    private boolean dgcaDeclaration;
    private boolean licenseVerified;
    private double hours;
    private long aircraftCost;
    private long pilotCost;
    private long crewCost;
    private long airportCharges;
    private long fuelSurcharge;
    private long gst;
    private long total;
    private String status;
    private String assignedPilotId;
    private String assignedCrewIds;
    private String createdAt;

    public Booking() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getTripType() { return tripType; }
    public void setTripType(String tripType) { this.tripType = tripType; }

    public String getOrigin() { return origin; }
    public void setOrigin(String origin) { this.origin = origin; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getReturnDate() { return returnDate; }
    public void setReturnDate(String returnDate) { this.returnDate = returnDate; }

    public String getReturnTime() { return returnTime; }
    public void setReturnTime(String returnTime) { this.returnTime = returnTime; }

    public int getPax() { return pax; }
    public void setPax(int pax) { this.pax = pax; }

    public String getAircraftId() { return aircraftId; }
    public void setAircraftId(String aircraftId) { this.aircraftId = aircraftId; }

    public String getAircraftModel() { return aircraftModel; }
    public void setAircraftModel(String aircraftModel) { this.aircraftModel = aircraftModel; }

    public boolean isSelfFly() { return selfFly; }
    public void setSelfFly(boolean selfFly) { this.selfFly = selfFly; }

    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }

    public String getLicenseClass() { return licenseClass; }
    public void setLicenseClass(String licenseClass) { this.licenseClass = licenseClass; }

    public int getFlyingHours() { return flyingHours; }
    public void setFlyingHours(int flyingHours) { this.flyingHours = flyingHours; }

    public String getCertificateFileName() { return certificateFileName; }
    public void setCertificateFileName(String certificateFileName) { this.certificateFileName = certificateFileName; }

    public boolean isDgcaDeclaration() { return dgcaDeclaration; }
    public void setDgcaDeclaration(boolean dgcaDeclaration) { this.dgcaDeclaration = dgcaDeclaration; }

    public boolean isLicenseVerified() { return licenseVerified; }
    public void setLicenseVerified(boolean licenseVerified) { this.licenseVerified = licenseVerified; }

    public double getHours() { return hours; }
    public void setHours(double hours) { this.hours = hours; }

    public long getAircraftCost() { return aircraftCost; }
    public void setAircraftCost(long aircraftCost) { this.aircraftCost = aircraftCost; }

    public long getPilotCost() { return pilotCost; }
    public void setPilotCost(long pilotCost) { this.pilotCost = pilotCost; }

    public long getCrewCost() { return crewCost; }
    public void setCrewCost(long crewCost) { this.crewCost = crewCost; }

    public long getAirportCharges() { return airportCharges; }
    public void setAirportCharges(long airportCharges) { this.airportCharges = airportCharges; }

    public long getFuelSurcharge() { return fuelSurcharge; }
    public void setFuelSurcharge(long fuelSurcharge) { this.fuelSurcharge = fuelSurcharge; }

    public long getGst() { return gst; }
    public void setGst(long gst) { this.gst = gst; }

    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getAssignedPilotId() { return assignedPilotId; }
    public void setAssignedPilotId(String assignedPilotId) { this.assignedPilotId = assignedPilotId; }

    public String getAssignedCrewIds() { return assignedCrewIds; }
    public void setAssignedCrewIds(String assignedCrewIds) { this.assignedCrewIds = assignedCrewIds; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
