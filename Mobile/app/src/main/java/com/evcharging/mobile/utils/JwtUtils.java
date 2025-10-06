package com.evcharging.mobile.utils;

import android.util.Base64;
import android.util.Log;

import com.evcharging.mobile.model.User;

import org.json.JSONObject;

/**
 * JwtUtils - Utility class for JWT token parsing and claim extraction
 *
 * Purpose: Extract user information from JWT tokens issued by the backend
 * Handles both EV Owners and CS Operators with nullable station fields
 *
 * Author: System (Enhanced)
 * Updated: 2025-10-06
 */
public class JwtUtils {

    private static final String TAG = "JwtUtils";

    /**
     * Get user role from JWT token
     *
     * @param token JWT token string
     * @return User role or null if not found
     */
    public static String getRoleFromToken(String token) {
        try {
            // JWT format: header.payload.signature
            String[] parts = token.split("\\.");
            if (parts.length < 2) return null;

            String payload = parts[1];
            byte[] decodedBytes = Base64.decode(payload, Base64.URL_SAFE);
            String decodedPayload = new String(decodedBytes);

            JSONObject json = new JSONObject(decodedPayload);
            return json.optString("role"); // assumes the JWT has a "role" claim
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Decode JWT token and extract payload as JSONObject
     *
     * @param token JWT token string
     * @return JSONObject containing token payload, or null if error
     */
    public static JSONObject decodeToken(String token) {
        try {
            if (token == null || token.isEmpty()) {
                Log.e(TAG, "Token is null or empty");
                return null;
            }

            // JWT format: header.payload.signature
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                Log.e(TAG, "Invalid JWT format. Expected 3 parts, got: " + parts.length);
                return null;
            }

            // Decode the payload (second part)
            String payload = parts[1];
            byte[] decodedBytes = Base64.decode(payload, Base64.URL_SAFE);
            String decodedPayload = new String(decodedBytes);

            Log.d(TAG, "JWT Decoded Payload: " + decodedPayload);

            return new JSONObject(decodedPayload);
        } catch (Exception e) {
            Log.e(TAG, "Error decoding JWT token: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Extract complete user information from JWT token
     * Creates a User object with all available claims
     *
     * @param token JWT token string
     * @return User object with extracted data, or null if error
     */
    public static User getUserFromToken(String token) {
        try {
            JSONObject payload = decodeToken(token);
            if (payload == null) {
                Log.e(TAG, "Failed to decode token payload");
                return null;
            }

            // Log all available claims for debugging
            Log.d(TAG, "Available JWT claims: " + payload.toString());

            // Create new User object
            User user = new User();

            // Extract user ID (can be in different claim names)
            // Common claim names: sub, nameid, NameIdentifier, userId, id
            String userId = null;
            if (payload.has("nameid")) {
                userId = payload.optString("nameid");
            } else if (payload.has("sub")) {
                userId = payload.optString("sub");
            } else if (payload.has("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/nameidentifier")) {
                userId = payload.optString("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/nameidentifier");
            } else if (payload.has("userId")) {
                userId = payload.optString("userId");
            }
            user.setUserId(userId);
            Log.d(TAG, "Extracted userId: " + userId);

            // Extract full name (can be in different claim names)
            String fullName = null;
            if (payload.has("name")) {
                fullName = payload.optString("name");
            } else if (payload.has("fullName")) {
                fullName = payload.optString("fullName");
            } else if (payload.has("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/name")) {
                fullName = payload.optString("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/name");
            }
            user.setFullName(fullName != null ? fullName : "User");
            Log.d(TAG, "Extracted fullName: " + fullName);

            // Extract email
            String email = null;
            if (payload.has("email")) {
                email = payload.optString("email");
            } else if (payload.has("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/emailaddress")) {
                email = payload.optString("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/emailaddress");
            }
            user.setEmail(email != null ? email : "");
            Log.d(TAG, "Extracted email: " + email);

            // Extract role
            String role = null;
            if (payload.has("role")) {
                role = payload.optString("role");
            } else if (payload.has("http://schemas.microsoft.com/ws/2008/06/identity/claims/role")) {
                role = payload.optString("http://schemas.microsoft.com/ws/2008/06/identity/claims/role");
            }
            user.setRole(role);
            Log.d(TAG, "Extracted role: " + role);

            // Extract station information (for operators - these fields may be null)
            String stationId = payload.optString("stationId", null);
            String stationName = payload.optString("stationName", null);
            String stationLocation = payload.optString("stationLocation", null);

            user.setStationId(stationId);
            user.setStationName(stationName);
            user.setStationLocation(stationLocation);

            if (stationId != null && !stationId.isEmpty()) {
                Log.d(TAG, "Station assigned - ID: " + stationId + ", Name: " + stationName);
            } else {
                Log.d(TAG, "No station assigned (operator may be waiting for admin assignment)");
            }

            // Extract isActive (default to true if not present)
            boolean isActive = payload.optBoolean("isActive", true);
            user.setActive(isActive);
            Log.d(TAG, "Extracted isActive: " + isActive);

            // Set current timestamp as createdAt (or extract if available)
            String createdAt = payload.optString("createdAt", String.valueOf(System.currentTimeMillis()));
            user.setCreatedAt(createdAt);

            Log.d(TAG, "User object created successfully from JWT");
            Log.d(TAG, "User details: " + user.toString());

            return user;

        } catch (Exception e) {
            Log.e(TAG, "Error extracting user from token: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Check if token is valid (not expired)
     * Note: This only checks expiration, not signature validity
     *
     * @param token JWT token string
     * @return true if token is valid, false otherwise
     */
    public static boolean isTokenValid(String token) {
        try {
            JSONObject payload = decodeToken(token);
            if (payload == null) return false;

            // Check expiration claim (exp)
            if (payload.has("exp")) {
                long exp = payload.getLong("exp");
                long currentTime = System.currentTimeMillis() / 1000; // Convert to seconds

                boolean isValid = exp > currentTime;
                Log.d(TAG, "Token expiration check - Expires at: " + exp + ", Current: " + currentTime + ", Valid: " + isValid);
                return isValid;
            }

            // If no exp claim, consider token valid
            Log.d(TAG, "No expiration claim found, assuming token is valid");
            return true;

        } catch (Exception e) {
            Log.e(TAG, "Error checking token validity: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Get specific claim from token
     *
     * @param token JWT token string
     * @param claimName Name of the claim to extract
     * @return Claim value as string, or null if not found
     */
    public static String getClaimFromToken(String token, String claimName) {
        try {
            JSONObject payload = decodeToken(token);
            if (payload == null) return null;

            return payload.optString(claimName, null);
        } catch (Exception e) {
            Log.e(TAG, "Error getting claim '" + claimName + "': " + e.getMessage(), e);
            return null;
        }
    }
}
