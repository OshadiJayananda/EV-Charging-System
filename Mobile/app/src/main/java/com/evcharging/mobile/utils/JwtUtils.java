package com.evcharging.mobile.utils;

import android.util.Base64;
import org.json.JSONObject;

public class JwtUtils {

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
}
