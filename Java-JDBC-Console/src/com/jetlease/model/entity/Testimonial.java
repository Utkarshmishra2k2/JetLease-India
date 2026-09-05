package com.jetlease.model.entity;

public class Testimonial {
    private String name;
    private String role;
    private String quote;

    public Testimonial() {}

    public Testimonial(String name, String role, String quote) {
        this.name = name;
        this.role = role;
        this.quote = quote;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getQuote() { return quote; }
    public void setQuote(String quote) { this.quote = quote; }
}
