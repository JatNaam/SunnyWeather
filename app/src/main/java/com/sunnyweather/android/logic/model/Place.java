package com.sunnyweather.android.logic.model;

import com.google.gson.annotations.SerializedName;

public class Place {
    private String name;
    private Location location;

    /*自己定义的java对象里的属性名跟json里的字段名不一样，所以使用SerializedName来匹配起来*/
    @SerializedName("formatted_address")
    private String address;

    public Place(String name, Location location, String address) {
        this.name = name;
        this.location = location;
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
