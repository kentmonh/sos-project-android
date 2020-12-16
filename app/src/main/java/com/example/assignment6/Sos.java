package com.example.assignment6;

public class Sos {
    private String name;
    private String mobilePhone;
    private String address;
    private String note;
    private double lat;
    private double lng;
    private String email;

    public Sos() {
    }

    public Sos(String name, String mobilePhone, String address, String note, double lat, double lng, String email) {
        this.name = name;
        this.mobilePhone = mobilePhone;
        this.address = address;
        this.note = note;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
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
