package com.evcharging.mobile.model;

public class Booking {
    private String id;
    private String evOwnerName;
    private String evOwnerEmail;
    private String stationId;
    private String slotTime;
    private String date;
    private String status;

    public Booking() {}

    public Booking(String id, String evOwnerName, String evOwnerEmail,
                   String stationId, String slotTime, String date, String status) {
        this.id = id;
        this.evOwnerName = evOwnerName;
        this.evOwnerEmail = evOwnerEmail;
        this.stationId = stationId;
        this.slotTime = slotTime;
        this.date = date;
        this.status = status;
    }

    // Getters
    public String getId() { return id; }
    public String getEvOwnerName() { return evOwnerName; }
    public String getEvOwnerEmail() { return evOwnerEmail; }
    public String getStationId() { return stationId; }
    public String getSlotTime() { return slotTime; }
    public String getDate() { return date; }
    public String getStatus() { return status; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setEvOwnerName(String evOwnerName) { this.evOwnerName = evOwnerName; }
    public void setEvOwnerEmail(String evOwnerEmail) { this.evOwnerEmail = evOwnerEmail; }
    public void setStationId(String stationId) { this.stationId = stationId; }
    public void setSlotTime(String slotTime) { this.slotTime = slotTime; }
    public void setDate(String date) { this.date = date; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return slotTime + " - " + evOwnerName + " - " + status;
    }
}
