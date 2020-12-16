package com.example.assignment6;

public class Safe {
    private String address;
    private double lat;
    private double lng;
    private String email;

    public Safe() {
    }

    public Safe(String address, double lat, double lng, String email) {
        this.address = address;
        this.lat = lat;
        this.lng = lng;
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String name) {
        this.address = name;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
