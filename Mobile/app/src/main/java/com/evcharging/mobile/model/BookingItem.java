package com.evcharging.mobile.model;

public class BookingItem {
    public String bookingId;     // Mongo _id as string (optional if backend returns)
    public String stationId;
    public String stationName;   // may be null from list; we fill in on details screen
    public String slotId;
    public int    slotNumber;    // from DB SlotNumber
    public String timeSlotId;
    public String ownerId;
    public String status;        // Pending/Approved/Charging/Finalized/Canceled/Expired
    public long   startTimeMs;   // epoch millis
    public long   endTimeMs;     // epoch millis
    public String qrCode;        // GUID
    public long   qrExpiresAtMs;
    public String qrImageBase64; // may be null on list
}
