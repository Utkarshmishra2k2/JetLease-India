package com.jetlease.model.entity;

public class Passenger {
    private int id;
    private String bookingId;
    private String name;
    private String dob;
    private String gender;
    private String aadhaar;
    private String verificationStatus;
    private boolean noAadhaar;
    private String altDocumentId;

    public Passenger() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDob() { return dob; }
    public void setDob(String dob) { this.dob = dob; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getAadhaar() { return aadhaar; }
    public void setAadhaar(String aadhaar) { this.aadhaar = aadhaar; }

    public String getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(String verificationStatus) { this.verificationStatus = verificationStatus; }

    public boolean isNoAadhaar() { return noAadhaar; }
    public void setNoAadhaar(boolean noAadhaar) { this.noAadhaar = noAadhaar; }

    public String getAltDocumentId() { return altDocumentId; }
    public void setAltDocumentId(String altDocumentId) { this.altDocumentId = altDocumentId; }
}
