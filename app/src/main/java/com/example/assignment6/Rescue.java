package com.example.assignment6;

public class Rescue {
    private String name;
    private String mobilePhone;
    private double lat;
    private double lng;

    public Rescue() {
    }

    public Rescue(String name, String mobilePhone, double lat, double lng) {
        this.name = name;
        this.mobilePhone = mobilePhone;
        this.lat = lat;
        this.lng = lng;
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
}
