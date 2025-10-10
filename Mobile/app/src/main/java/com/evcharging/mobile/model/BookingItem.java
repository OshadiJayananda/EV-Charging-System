package com.evcharging.mobile.model;

public class BookingItem {

    // --- Fields ---
    private String bookingId;
    private String stationId;
    private String stationName;
    private String slotId;
    private String slotNumber;
    private String timeSlotId;
    private String ownerId;
    private String status;
    private String startTime;
    private String endTime;
    private String qrImageBase64;

    private String cancellationReason;




    // --- Getters ---
    public String getBookingId() { return bookingId; }
    public String getStationId() { return stationId; }
    public String getStationName() { return stationName; }
    public String getSlotId() { return slotId; }
    public String getSlotNumber() { return slotNumber; }
    public String getTimeSlotId() { return timeSlotId; }
    public String getOwnerId() { return ownerId; }
    public String getStatus() { return status; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public String getQrImageBase64() { return qrImageBase64; }
    public String getCancellationReason() {
        return cancellationReason;
    }



    // --- Setters ---
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }
    public void setStationId(String stationId) { this.stationId = stationId; }
    public void setStationName(String stationName) { this.stationName = stationName; }
    public void setSlotId(String slotId) { this.slotId = slotId; }
    public void setSlotNumber(String slotNumber) { this.slotNumber = slotNumber; }
    public void setTimeSlotId(String timeSlotId) { this.timeSlotId = timeSlotId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
    public void setStatus(String status) { this.status = status; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public void setQrImageBase64(String qrImageBase64) { this.qrImageBase64 = qrImageBase64; }
    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    // --- Helper formatted display ---
    public String getStartTimeFormatted() {
        try {
            return formatTime(startTime);
        } catch (Exception e) {
            return startTime;
        }
    }

    public String getEndTimeFormatted() {
        try {
            return formatTime(endTime);
        } catch (Exception e) {
            return endTime;
        }
    }

    private String formatTime(String utcTime) {
        try {
            java.text.SimpleDateFormat inFmt = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault());
            inFmt.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));

            // if input has milliseconds or Z timezone
            if (utcTime.endsWith("Z")) {
                inFmt = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.getDefault());
                inFmt.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
            }

            java.text.SimpleDateFormat outFmt = new java.text.SimpleDateFormat("dd MMM yyyy, h:mm a", java.util.Locale.getDefault());
            java.util.Date d = inFmt.parse(utcTime);
            return outFmt.format(d);
        } catch (Exception e) {
            e.printStackTrace();
            return utcTime;
        }
    }

}
