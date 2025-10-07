package com.evcharging.mobile.model;

public class User {

    // User identification
    private String userId; // NIC from JWT (unique identifier)
    private String fullName; // User's full name
    private String email; // User's email address
    private String role; // User role: "Owner", "Operator", "Admin", etc.

    // Station information (for CS Operators only - nullable)
    private String stationId; // ID of assigned station (null if not assigned)
    private String stationName; // Name of assigned station (null if not assigned)
    private String stationLocation; // Location of assigned station (null if not assigned)

    // User status
    private boolean isActive; // Whether user account is active
    private String createdAt; // Account creation timestamp
    private String phone; // User phone number
    private boolean reactivationRequested; // Flag for reactivation request

    /**
     * Default constructor
     */
    public User() {
    }

    /**
     * Full constructor for users with station
     */
    public User(String userId, String fullName, String email, String role,
            String stationId, String stationName, String stationLocation,
            boolean isActive, String createdAt) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.stationId = stationId;
        this.stationName = stationName;
        this.stationLocation = stationLocation;
        this.isActive = isActive;
        this.createdAt = createdAt;
    }

    /**
     * Constructor for users without station
     */
    public User(String userId, String fullName, String email, String role,
            boolean isActive, String createdAt, String phone, boolean reactivationRequested) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.stationId = null;
        this.stationName = null;
        this.stationLocation = null;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.phone = phone;
        this.reactivationRequested = reactivationRequested;
    }

    // Getters & Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStationId() {
        return stationId;
    }

    public void setStationId(String stationId) {
        this.stationId = stationId;
    }

    public String getStationName() {
        return stationName;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    public String getStationLocation() {
        return stationLocation;
    }

    public void setStationLocation(String stationLocation) {
        this.stationLocation = stationLocation;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean isReactivationRequested() {
        return reactivationRequested;
    }

    public void setReactivationRequested(boolean reactivationRequested) {
        this.reactivationRequested = reactivationRequested;
    }

    // Utility methods
    public boolean isOperator() {
        return role != null && role.equalsIgnoreCase("operator");
    }

    public boolean isOwner() {
        return role != null && role.equalsIgnoreCase("owner");
    }

    public boolean isAdmin() {
        return role != null && role.equalsIgnoreCase("admin");
    }

    public boolean hasStationAssigned() {
        return stationId != null && !stationId.trim().isEmpty();
    }

    public String getStationDisplayInfo() {
        if (hasStationAssigned())
            return stationName + " (" + stationId + ")";
        return "Not Assigned";
    }

    public String getRoleDisplayName() {
        if (role == null)
            return "Unknown";
        String normalized = role.toLowerCase();
        if (normalized.equals("csoperator") || normalized.equals("operator"))
            return "Operator";
        if (normalized.equals("owner"))
            return "Owner";
        if (normalized.equals("admin"))
            return "Admin";
        return role.substring(0, 1).toUpperCase() + role.substring(1).toLowerCase();
    }

    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", stationId='" + stationId + '\'' +
                ", stationName='" + stationName + '\'' +
                ", stationLocation='" + stationLocation + '\'' +
                ", isActive=" + isActive +
                ", createdAt='" + createdAt + '\'' +
                ", phone='" + phone + '\'' +
                ", reactivationRequested=" + reactivationRequested +
                '}';
    }
}
