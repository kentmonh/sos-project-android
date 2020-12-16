package com.example.assignment6;

public class Rescue {
    private String name;
    private String mobilePhone;
    private double lat;
    private double lng;
    private String email;

    public Rescue() {
    }

    public Rescue(String name, String mobilePhone, double lat, double lng, String email) {
        this.name = name;
        this.mobilePhone = mobilePhone;
        this.lat = lat;
        this.lng = lng;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMobilePhone() {
        return mobilePhone;
    }

    public void setMobilePhone(String mobilePhone) {
        this.mobilePhone = mobilePhone;
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
