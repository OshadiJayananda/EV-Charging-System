package com.evcharging.mobile.network;

import android.util.Log;

import com.evcharging.mobile.session.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ApiClient {
    private static final String TAG = "ApiClient";
    private static final String BASE_URL = "http://10.0.2.2:5000/api";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private OkHttpClient client;
    private SessionManager sessionManager;

    public ApiClient(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    // Login
    public ApiResponse login(String email, String password) {
        try {
            JSONObject loginData = new JSONObject();
            loginData.put("email", email);
            loginData.put("password", password);

            RequestBody body = RequestBody.create(loginData.toString(), JSON);
            Request request = new Request.Builder()
                    .url(BASE_URL + "/auth/login")
                    .post(body)
                    .build();

            Response response = client.newCall(request).execute();
            String responseBody = response.body().string();

            Log.d(TAG, "Login response: " + responseBody);

            if (response.isSuccessful()) {
                JSONObject jsonResponse = new JSONObject(responseBody);
                String token = jsonResponse.getString("token");
                sessionManager.saveToken(token);
                return new ApiResponse(true, "Login successful", token);
            } else {
                JSONObject errorResponse = new JSONObject(responseBody);
                String errorMessage = errorResponse.optString("message", "Login failed");
                return new ApiResponse(false, errorMessage, null);
            }

        } catch (IOException | JSONException e) {
            Log.e(TAG, "Error during login", e);
            return new ApiResponse(false, "Network or parsing error", null);
        }
    }

    // GET request
    public ApiResponse get(String endpoint) {
        try {
            Request.Builder requestBuilder = new Request.Builder()
                    .url(BASE_URL + endpoint)
                    .get();

            String token = sessionManager.getToken();
            if (token != null) {
                requestBuilder.addHeader("Authorization", "Bearer " + token);
            }

            Response response = client.newCall(requestBuilder.build()).execute();
            String responseBody = response.body().string();

            if (response.isSuccessful()) {
                return new ApiResponse(true, "Success", responseBody);
            } else {
                JSONObject errorResponse = new JSONObject(responseBody);
                return new ApiResponse(false, errorResponse.optString("message", "Request failed"), null);
            }

        } catch (Exception e) {
            Log.e(TAG, "GET request error", e);
            return new ApiResponse(false, "Network error occurred", null);
        }
    }

    // POST request
    public ApiResponse post(String endpoint, JSONObject data) {
        try {
            RequestBody body = RequestBody.create(data.toString(), JSON);
            Request.Builder requestBuilder = new Request.Builder()
                    .url(BASE_URL + endpoint)
                    .post(body);

            String token = sessionManager.getToken();
            if (token != null) {
                requestBuilder.addHeader("Authorization", "Bearer " + token);
            }

            Response response = client.newCall(requestBuilder.build()).execute();
            String responseBody = response.body().string();

            if (response.isSuccessful()) {
                return new ApiResponse(true, "Success", responseBody);
            } else {
                JSONObject errorResponse = new JSONObject(responseBody);
                return new ApiResponse(false, errorResponse.optString("message", "Request failed"), null);
            }

        } catch (Exception e) {
            Log.e(TAG, "POST request error", e);
            return new ApiResponse(false, "Network error occurred", null);
        }
    }

    // Logout
    public ApiResponse logout() {
        try {
            post("/auth/logout", new JSONObject());
        } catch (Exception e) {
            Log.w(TAG, "Logout API failed, clearing token anyway", e);
        }
        sessionManager.clearToken();
        return new ApiResponse(true, "Logged out successfully", null);
    }
}
