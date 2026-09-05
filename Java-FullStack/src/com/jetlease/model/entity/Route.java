package com.jetlease.model.entity;

public class Route {
    private String code;
    private String city;
    private double lat;
    private double lng;

    public Route() {}

    public Route(String code, String city, double lat, double lng) {
        this.code = code;
        this.city = city;
        this.lat = lat;
        this.lng = lng;
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public double getLat() { return lat; }
    public void setLat(double lat) { this.lat = lat; }

    public double getLng() { return lng; }
    public void setLng(double lng) { this.lng = lng; }
}