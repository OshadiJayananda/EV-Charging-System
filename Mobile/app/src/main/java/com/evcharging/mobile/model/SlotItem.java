package com.evcharging.mobile.model;

public class SlotItem {
    public String slotId;
    public String number;     // "Slot 01", etc.
    public String status;     // "Available", etc.
    public String connectorType;

    @Override public String toString() {
        return (number != null ? number : "Slot") + (connectorType != null ? " • " + connectorType : "");
    }
}
