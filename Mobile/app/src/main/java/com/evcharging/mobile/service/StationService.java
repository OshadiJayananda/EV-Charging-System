package com.evcharging.mobile.service;

import android.util.Log;

import com.evcharging.mobile.model.Station;
import com.evcharging.mobile.network.ApiClient;
import com.evcharging.mobile.network.ApiResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.List;

public class StationService {
    private static final String TAG = "StationService";
    private static final Logger log = LoggerFactory.getLogger(StationService.class);
    private final ApiClient apiClient;
    private final Gson gson;

    public StationService(ApiClient apiClient) {
        this.apiClient = apiClient;
        this.gson = new Gson();
    }

    public List<Station> getNearbyStations(double latitude, double longitude, double radiusKm) {
        try {
            Log.d("OwnerHomeActivity", "Calling getNearbyStations...");

            // Note: your BASE_URL already includes /api
            String endpoint = String.format("/station/nearby?latitude=%f&longitude=%f&radiusKm=%f",
                    latitude, longitude, radiusKm);

            Log.d(TAG, "Nearby stations endpoint: " + endpoint);

            ApiResponse response = apiClient.get(endpoint);
            Log.d(TAG, "Success: " + response.isSuccess());
            Log.d(TAG, "Message: " + response.getMessage());
            Log.d(TAG, "Data: " + response.getData());
            if (response == null || !response.isSuccess()) {
                Log.e(TAG, "Failed to fetch stations: " + (response != null ? response.getMessage() : "null response"));
                return null;
            }

            String jsonData = response.getData(); // Raw JSON string from ApiResponse
            if (jsonData == null || jsonData.isEmpty()) {
                Log.e(TAG, "Empty station data");
                return null;
            }

            Type listType = new TypeToken<List<Station>>() {}.getType();
            return gson.fromJson(jsonData, listType);

        } catch (Exception e) {
            Log.e(TAG, "Error fetching nearby stations", e);
            return null;
        }
    }
}
