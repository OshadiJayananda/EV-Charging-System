package com.evcharging.mobile.model;

public class Station {
    private String stationId;
    private String name;
    private double latitude;
    private double longitude;
    private String location;
    private String type;

    // ---- Getters ----
    public String getStationId() { return stationId; }
    public String getName() { return name; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getLocation() { return location; }
    public String getType() { return type; }

    // ---- Setters ----
    public void setStationId(String stationId) { this.stationId = stationId; }
    public void setName(String name) { this.name = name; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public void setLocation(String location) { this.location = location; }
    public void setType(String type) { this.type = type; }

    @Override
    public String toString() {
        return name + " - " + location;
    }
}
