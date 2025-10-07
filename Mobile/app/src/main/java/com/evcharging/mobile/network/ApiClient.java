package com.evcharging.mobile.network;

import android.util.Log;
import com.evcharging.mobile.model.Notification;
import com.evcharging.mobile.session.SessionManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ApiClient {
    private static final String TAG = "ApiClient";
    private static final String BASE = "https://87ce258e3dbe.ngrok-free.app";
    private static final String BASE_URL = BASE + "/api";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private OkHttpClient client;
    private SessionManager sessionManager;
    private Gson gson;

    public ApiClient(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
        this.gson = new Gson();

        // Create OkHttpClient with unsafe SSL for ngrok (development only)
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .hostnameVerifier((hostname, session) -> true)
                .sslSocketFactory(getUnsafeSslContext().getSocketFactory(), getTrustAllCertsManager())
                .build();
    }

    private SSLContext getUnsafeSslContext() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[] { getTrustAllCertsManager() };
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            return sslContext;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private X509TrustManager getTrustAllCertsManager() {
        return new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[] {};
            }
        };
    }

    // Static method to get base URL for other services
    public static String getBaseUrl() {
        return BASE;
    }

    // Static method to get API base URL
    public static String getApiBaseUrl() {
        return BASE_URL;
    }

    // Notification API Methods
    public ApiResponse getUserNotifications() {
        return get("/notifications/user");
    }

    public ApiResponse markNotificationAsRead(String notificationId) {
        try {
            String endpoint = "/notifications/" + notificationId + "/read";
            return patch(endpoint, null);
        } catch (Exception e) {
            Log.e(TAG, "Error marking notification as read", e);
            return new ApiResponse(false, "Request creation error", null);
        }
    }

    public ApiResponse deleteNotification(String notificationId) {
        return delete("/notifications/" + notificationId);
    }

    // Helper method to parse notifications list
    public List<Notification> parseNotifications(String json) {
        try {
            Type listType = new TypeToken<List<Notification>>() {
            }.getType();
            return gson.fromJson(json, listType);
        } catch (Exception e) {
            Log.e(TAG, "Error parsing notifications", e);
            return null;
        }
    }

    // Login
    public ApiResponse login(String email, String password) {
        try {
            // Prepare JSON payload
            JSONObject loginData = new JSONObject();
            loginData.put("email", email);
            loginData.put("password", password);

            RequestBody body = RequestBody.create(loginData.toString(), JSON);
            Request request = new Request.Builder()
                    .url(BASE_URL + "/auth/login")
                    .addHeader("X-Client-Type", "Mobile")   
                    .post(body)
                    .build();

            Response response = client.newCall(request).execute();
            int statusCode = response.code();
            Log.d(TAG, "Login response code: " + statusCode);
            String responseBody = response.body() != null ? response.body().string() : "";

            Log.d(TAG, "Login response code: " + statusCode);
            Log.d(TAG, "Login response body: '" + responseBody + "'");

            // Handle based on status code
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

                case 204: // No Content
                    return new ApiResponse(true, "Login successful (no content returned)", null);

                case 401: // Unauthorized
                
                case 403: // Forbidden (Access denied from this platform)
                    String errMsg = "Access denied: You are not allowed to log in from this app.";
                    if (!responseBody.isEmpty()) {
                        try {
                            JSONObject errorJson = new JSONObject(responseBody);
                            String msg = errorJson.optString("message", null);
                            if (msg != null && !msg.isEmpty()) errMsg = msg;
                        } catch (Exception ignored) {}
                    }
                    return new ApiResponse(false, errMsg, null);

                case 404: // Not Found
                    return new ApiResponse(false, "Login endpoint not found", null);

                default: // Other errors
                    if (!responseBody.isEmpty()) {
                        try {
                            JSONObject errorResponse = new JSONObject(responseBody);
                            String message = errorResponse.optString("message", "Unknown error");
                            return new ApiResponse(false, message, null);
                        } catch (JSONException e) {
                            // Response not JSON
                            return new ApiResponse(false, "Unexpected error occurred! Contact Administration", null);
                        }
                    } else {
                        return new ApiResponse(false, "Unknown error occurred. Status code: " + statusCode, null);
                    }
            }

        } catch (IOException e) {
            Log.e(TAG, "Network error during login", e);
            return new ApiResponse(false, "Network error occurred", null);
        } catch (JSONException e) {
            Log.e(TAG, "JSON parsing error during login", e);
            return new ApiResponse(false, "Response parsing error", null);
        }
    }

    // register
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
                    .addHeader("X-Client-Type", "Mobile")
                    .build();
            Response response = client.newCall(request).execute();
            String responseBody = response.body() != null ? response.body().string() : "";
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
                    .get()
                    .addHeader("X-Client-Type", "Mobile");


            String token = sessionManager.getToken();
            if (token != null) {
                requestBuilder.addHeader("Authorization", "Bearer " + token);
            }
            Log.e(TAG, "GET Request url: " + BASE_URL + endpoint);

            Response response = client.newCall(requestBuilder.build()).execute();
            Log.e(TAG, "GET Response code: " + response.code());

            String responseBody = response.body() != null ? response.body().string() : "";
            Log.e(TAG, "GET Response body: " + response.body());

            if (response.isSuccessful()) {
                return new ApiResponse(true, "Success", responseBody);
            } else {
                if (responseBody == null || responseBody.isEmpty()) {
                    Log.e(TAG, "Empty response for endpoint: " + endpoint);
                    return new ApiResponse(false, "Empty response from server (code " + response.code() + ")", null);
                }
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
                    .post(body)
                    .addHeader("X-Client-Type", "Mobile");


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

    // Patch request
    public ApiResponse patch(String endpoint, JSONObject data) {
        try {
            RequestBody body = data != null
                    ? RequestBody.create(data.toString(), JSON)
                    : RequestBody.create("", JSON); // empty body if none provided

            Request.Builder requestBuilder = new Request.Builder()
                    .url(BASE_URL + endpoint)
                    .patch(body)
                    .addHeader("X-Client-Type", "Mobile");


            String token = sessionManager.getToken();
            if (token != null) {
                requestBuilder.addHeader("Authorization", "Bearer " + token);
            }

            Response response = client.newCall(requestBuilder.build()).execute();
            String responseBody = response.body() != null ? response.body().string() : "";

            if (response.isSuccessful()) {
                return new ApiResponse(true, "Success", responseBody);
            } else {
                JSONObject errorResponse = new JSONObject(responseBody);
                return new ApiResponse(false, errorResponse.optString("message", "Request failed"), null);
            }
        } catch (Exception e) {
            Log.e(TAG, "PATCH request error", e);
            return new ApiResponse(false, "Network error occurred", null);
        }
    }

    public ApiResponse delete(String endpoint) {
        try {
            Request.Builder requestBuilder = new Request.Builder()
                    .url(BASE_URL + endpoint)
                    .delete()
                    .addHeader("X-Client-Type", "Mobile");


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
            Log.e(TAG, "DELETE request error", e);
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

    public ApiResponse logoutAndForget() {
        try {
            post("/auth/logout", new JSONObject());
        } catch (Exception e) {
            Log.w(TAG, "Logout API failed, clearing all data anyway", e);
        }

        sessionManager.clearAll();
        return new ApiResponse(true, "Logged out and credentials cleared", null);
    }

    public ApiResponse updateEvOwner(String nic, JSONObject data) {
        try {
            RequestBody body = RequestBody.create(data.toString(), JSON);
            Request.Builder requestBuilder = new Request.Builder()
                    .url(BASE_URL + "/owners/" + nic)
                    .put(body);

            String token = sessionManager.getToken();
            if (token != null) {
                requestBuilder.addHeader("Authorization", "Bearer " + token);
            }

            Response response = client.newCall(requestBuilder.build()).execute();
            String responseBody = response.body().string();

            if (response.isSuccessful()) {
                return new ApiResponse(true, "Profile updated successfully", responseBody);
            } else {
                JSONObject errorResponse = new JSONObject(responseBody);
                return new ApiResponse(false, errorResponse.optString("message", "Update failed"), null);
            }
        } catch (Exception e) {
            Log.e(TAG, "Update error", e);
            return new ApiResponse(false, "Network error occurred", null);
        }
    }

    // Generic PATCH helper
    private ApiResponse patch(String endpoint) {
        try {
            Request.Builder requestBuilder = new Request.Builder()
                    .url(BASE_URL + endpoint)
                    .patch(RequestBody.create("", JSON))
                    .addHeader("X-Client-Type", "Mobile");


            String token = sessionManager.getToken();
            if (token != null)
                requestBuilder.addHeader("Authorization", "Bearer " + token);

            Response response = client.newCall(requestBuilder.build()).execute();
            String responseBody = response.body().string();

            if (response.isSuccessful()) {
                return new ApiResponse(true, "Operation successful", responseBody);
            } else {
                JSONObject errorResponse = new JSONObject(responseBody);
                Log.e(TAG, "PATCH response status code " + response.code() + " response body" + responseBody);
                return new ApiResponse(false, errorResponse.optString("message", "Request failed"), null);
            }
        } catch (Exception e) {
            Log.e(TAG, "PATCH request error", e);
            return new ApiResponse(false, "Network error occurred", null);
        }
    }

    // Generic PUT helper
    private ApiResponse put(String endpoint, JSONObject data) {
        try {
            RequestBody body = RequestBody.create(data.toString(), JSON);
            Request.Builder requestBuilder = new Request.Builder()
                    .url(BASE_URL + endpoint)
                    .put(body)
                    .addHeader("X-Client-Type", "Mobile");


            String token = sessionManager.getToken();
            if (token != null)
                requestBuilder.addHeader("Authorization", "Bearer " + token);

            Response response = client.newCall(requestBuilder.build()).execute();
            String responseBody = response.body().string();

            if (response.isSuccessful()) {
                return new ApiResponse(true, "Operation successful", responseBody);
            } else {
                JSONObject errorResponse = new JSONObject(responseBody);
                return new ApiResponse(false, errorResponse.optString("message", "Request failed"), null);
            }
        } catch (Exception e) {
            Log.e(TAG, "PUT request error", e);
            return new ApiResponse(false, "Network error occurred", null);
        }
    }


    // Deactivate EV Owner
    public ApiResponse deactivateEvOwner(String nic) {
        return patch("/owners/" + nic + "/deactivate");
    }

    // Request Reactivation
    public ApiResponse requestReactivation(String nic) {
        return patch("/owners/" + nic + "/request-reactivation");
    }

    // Fetch bookings by station
    public ApiResponse getBookingsByStation(String stationId) {
        try {
            if (stationId == null || stationId.isEmpty() || stationId.equals("string")) {
                return new ApiResponse(false, "No station assigned", null);
            }

            String endpoint = "/bookings/station/" + stationId;
            Log.d(TAG, "Fetching bookings for station: " + stationId);
            return get(endpoint);
        } catch (Exception e) {
            Log.e(TAG, "Error fetching bookings by station", e);
            return new ApiResponse(false, "Error fetching bookings", null);
        }
    }

}
