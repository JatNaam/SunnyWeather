package com.sunnyweather.android.logic.model;

public class Location {
    private String lng;
    private String lat;
    Location(String lng, String lat){
        this.lng = lng;
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }
}
