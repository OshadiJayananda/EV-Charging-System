package com.evcharging.mobile.model;

/**
 * User - Model class representing a user in the EV Charging system
 *
 * Purpose: Local representation of user data extracted from JWT and stored in SQLite
 * Supports both EV Owners and CS Operators with nullable station fields for operators
 * who haven't been assigned to a station yet.
 *
 * Author: System
 * Created: 2025-10-06
 */
public class User {

    // User identification
    private String userId;          // NIC from JWT (unique identifier)
    private String fullName;        // User's full name
    private String email;           // User's email address
    private String role;            // User role: "Owner", "Operator", "Admin", etc.

    // Station information (for CS Operators only - nullable)
    private String stationId;       // ID of assigned station (null if not assigned)
    private String stationName;     // Name of assigned station (null if not assigned)
    private String stationLocation; // Location of assigned station (null if not assigned)

    // User status
    private boolean isActive;       // Whether user account is active
    private String createdAt;       // Account creation timestamp

    /**
     * Default constructor
     */
    public User() {
        // Empty constructor for flexibility
    }

    /**
     * Full constructor for creating user with all details
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
     * Constructor for users without station (EV Owners or unassigned operators)
     */
    public User(String userId, String fullName, String email, String role,
                boolean isActive, String createdAt) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.stationId = null;
        this.stationName = null;
        this.stationLocation = null;
        this.isActive = isActive;
        this.createdAt = createdAt;
    }

    // Getters and Setters

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

    // Utility methods

    /**
     * Check if user is an operator
     *
     * @return true if role is "Operator" (case-insensitive)
     */
    public boolean isOperator() {
        return role != null && role.equalsIgnoreCase("operator");
    }

    /**
     * Check if user is an EV Owner
     *
     * @return true if role is "Owner" (case-insensitive)
     */
    public boolean isOwner() {
        return role != null && role.equalsIgnoreCase("owner");
    }

    /**
     * Check if user is an admin
     *
     * @return true if role is "Admin" (case-insensitive)
     */
    public boolean isAdmin() {
        return role != null && role.equalsIgnoreCase("admin");
    }

    /**
     * Check if operator has station assigned
     * Only relevant for operators
     *
     * @return true if stationId is not null and not empty
     */
    public boolean hasStationAssigned() {
        return stationId != null && !stationId.trim().isEmpty();
    }

    /**
     * Get display name for station info
     * Returns formatted station info or "Not Assigned" if no station
     *
     * @return Formatted station string
     */
    public String getStationDisplayInfo() {
        if (hasStationAssigned()) {
            return stationName + " (" + stationId + ")";
        }
        return "Not Assigned";
    }

    /**
     * Get short role name for display
     *
     * @return "Operator", "Owner", "Admin" or the role as-is
     */
    public String getRoleDisplayName() {
        if (role == null) return "Unknown";

        // Capitalize first letter for display
        String normalized = role.toLowerCase();
        if (normalized.equals("csoperator")) return "Operator";
        if (normalized.equals("operator")) return "Operator";
        if (normalized.equals("owner")) return "Owner";
        if (normalized.equals("admin")) return "Admin";

        // Return as-is with first letter capitalized
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
                '}';
    }
}
