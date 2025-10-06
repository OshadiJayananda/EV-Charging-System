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
    private static final String BASE = "https://dcc8d0c08176.ngrok-free.app"; // Ensure ngrok is active

    private static final String BASE_URL = BASE + "/api";
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

    // Static method to get the Base URL for API requests
    public static String getBaseUrl() {
        return BASE_URL;
    }

    // Login API call
    public ApiResponse login(String email, String password) {
        try {
            // Prepare JSON payload for login
            JSONObject loginData = new JSONObject();
            loginData.put("email", email);
            loginData.put("password", password);

            // Prepare request body
            RequestBody body = RequestBody.create(loginData.toString(), JSON);

            // Prepare the request
            Request request = new Request.Builder()
                    .url(BASE_URL + "/auth/login")
                    .post(body)
                    .build();

            // Execute request
            Response response = client.newCall(request).execute();
            int statusCode = response.code();
            String responseBody = response.body() != null ? response.body().string() : "";

            Log.d(TAG, "Login response code: " + statusCode);
            Log.d(TAG, "Login response body: '" + responseBody + "'");

            // Handle response based on status code
            switch (statusCode) {
                case 200: // OK
                    if (!responseBody.isEmpty()) {
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        String token = jsonResponse.optString("token", null);
                        if (token != null) {
                            sessionManager.saveToken(token);
                            return new ApiResponse(true, "Login successful", token);
                        } else {
                            return new ApiResponse(false, "Login failed: no token received", null);
                        }
                    } else {
                        return new ApiResponse(false, "Login failed: empty response", null);
                    }

                case 401: // Unauthorized
                    return new ApiResponse(false, "Unauthorized: Invalid email or password", null);

                default: // Other errors
                    if (!responseBody.isEmpty()) {
                        try {
                            JSONObject errorResponse = new JSONObject(responseBody);
                            String message = errorResponse.optString("message", "Unknown error");
                            return new ApiResponse(false, message, null);
                        } catch (JSONException e) {
                            // If the response is not a valid JSON
                            return new ApiResponse(false, "Unexpected error occurred! Contact Administration", null);
                        }
                    } else {
                        return new ApiResponse(false, "Unknown error occurred. Status code: " + statusCode, null);
                    }
            }

        } catch (IOException | JSONException e) {
            Log.e(TAG, "Network or JSON error during login", e);
            return new ApiResponse(false, "Network or JSON error occurred", null);
        }
    }

    // Register API call
    public ApiResponse register(String name, String email, String password) {
        try {
            JSONObject registerData = new JSONObject();
            registerData.put("name", name);
            registerData.put("email", email);
            registerData.put("password", password);
            RequestBody body = RequestBody.create(registerData.toString(), JSON);
            Request request = new Request.Builder()
                    .url(BASE_URL + "/auth/register")
                    .post(body)
                    .build();
            Response response = client.newCall(request).execute();
            String responseBody = response.body().string();
            if (response.isSuccessful()) {
                return new ApiResponse(true, "Registration successful", null);
            } else {
                JSONObject errorResponse = new JSONObject(responseBody);
                String errorMessage = errorResponse.optString("message", "Registration failed");
                return new ApiResponse(false, errorMessage, null);
            }
        } catch (IOException | JSONException e) {
            Log.e(TAG, "Error during registration", e);
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

    // Clear all session data (login credentials)
    public ApiResponse logoutAndForget() {
        try {
            post("/auth/logout", new JSONObject());
        } catch (Exception e) {
            Log.w(TAG, "Logout API failed, clearing all data anyway", e);
        }

        sessionManager.clearAll();
        return new ApiResponse(true, "Logged out and credentials cleared", null);
    }
}
