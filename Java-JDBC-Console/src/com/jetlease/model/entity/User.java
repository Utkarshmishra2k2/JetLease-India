package com.jetlease.model.entity;

public class User {
    private String id;
    private String fullName;
    private String email;
    private String phone;
    private String dob;
    private String emergencyContact;
    private String password;
    private String country;
    private String role;
    private String status;
    private String membership;
    private int loyaltyPoints;
    private String createdAt;

    public User() {}

    public User(String id, String fullName, String email, String phone, String dob, String emergencyContact,
                String password, String country, String role, String status, String membership, int loyaltyPoints, String createdAt) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.dob = dob;
        this.emergencyContact = emergencyContact;
        this.password = password;
        this.country = country;
        this.role = role;
        this.status = status;
        this.membership = membership;
        this.loyaltyPoints = loyaltyPoints;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getDob() { return dob; }
    public void setDob(String dob) { this.dob = dob; }

    public String getEmergencyContact() { return emergencyContact; }
    public void setEmergencyContact(String emergencyContact) { this.emergencyContact = emergencyContact; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMembership() { return membership; }
    public void setMembership(String membership) { this.membership = membership; }

    public int getLoyaltyPoints() { return loyaltyPoints; }
    public void setLoyaltyPoints(int loyaltyPoints) { this.loyaltyPoints = loyaltyPoints; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
