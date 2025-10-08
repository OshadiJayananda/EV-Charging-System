package com.evcharging.mobile.service;

import android.util.Log;

import com.evcharging.mobile.network.ApiClient;
import com.evcharging.mobile.network.ApiResponse;

import org.json.JSONException;
import org.json.JSONObject;

public class BookingService {
    private static final String TAG = "BookingService";
    private ApiClient apiClient;

    public BookingService(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiResponse createBooking(String ownerId, String slotId, String stationId, String startTime,
            String endTime) {

        JSONObject body = new JSONObject();
        try {
            body.put("ownerId", ownerId);
            body.put("slotId", slotId);
            body.put("stationId", stationId);
            body.put("startTime", startTime);
            body.put("endTime", endTime);
        } catch (JSONException e) {
            Log.e(TAG, "Failed to create booking JSON", e);
        }

        // POST request to backend
        return apiClient.post("/bookings", body);
    }
}
