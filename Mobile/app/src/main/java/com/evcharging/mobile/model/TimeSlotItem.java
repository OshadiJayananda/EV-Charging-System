package com.evcharging.mobile.model;

public class TimeSlotItem {
    public String timeSlotId;     // for /timeslot? returns 'timeSlotId'
    public String formattedStart; // when available
    public String formattedEnd;   // when available
    public String status;         // "Available"/"Booked"
    public String startTime;      // ISO
    public String endTime;        // ISO

    @Override public String toString() {
        if (formattedStart != null && formattedEnd != null) return formattedStart + " → " + formattedEnd;
        return (startTime != null ? startTime : "?") + " → " + (endTime != null ? endTime : "?");
    }
}
