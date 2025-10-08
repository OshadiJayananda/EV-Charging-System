package com.evcharging.mobile.network;

import android.util.Log;

import com.evcharging.mobile.model.Notification;
import com.evcharging.mobile.model.User;
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

/**
 * ApiClient – Handles all network requests for EV Charging Mobile App
 * 🔹 Includes clean logging for request + response (pretty JSON)
 * 🔹 Safe with ngrok SSL (for dev)
 * 🔹 Keeps all endpoints intact
 */
public class ApiClient {
    private static final String TAG = "ApiClient";
    private static final String BASE = "https://ee327572ee40.ngrok-free.app";
    private static final String BASE_URL = BASE + "/api";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private OkHttpClient client;
    private SessionManager sessionManager;
    private Gson gson;

    public ApiClient(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
        this.gson = new Gson();

        // ✅ Allow HTTPS (ngrok) connections during development
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .hostnameVerifier((hostname, session) -> true)
                .sslSocketFactory(getUnsafeSslContext().getSocketFactory(), getTrustAllCertsManager())
                .build();
    }

    // ---------------------------------------------------------------------
    // SSL: Disable certificate validation for ngrok (development only)
    // ---------------------------------------------------------------------
    private SSLContext getUnsafeSslContext() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{getTrustAllCertsManager()};
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            return sslContext;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private X509TrustManager getTrustAllCertsManager() {
        return new X509TrustManager() {
            @Override public void checkClientTrusted(X509Certificate[] chain, String authType) {}
            @Override public void checkServerTrusted(X509Certificate[] chain, String authType) {}
            @Override public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[]{}; }
        };
    }

    public static String getBaseUrl() { return BASE; }
    public static String getApiBaseUrl() { return BASE_URL; }

    // ---------------------------------------------------------------------
    // NOTIFICATIONS
    // ---------------------------------------------------------------------
    public ApiResponse getUserNotifications() {
        return get("/notifications/user");
    }

    public ApiResponse markNotificationAsRead(String id) {
        return patch("/notifications/" + id + "/read", null);
    }

    public ApiResponse deleteNotification(String id) {
        return delete("/notifications/" + id);
    }

    public List<Notification> parseNotifications(String json) {
        try {
            Type listType = new TypeToken<List<Notification>>() {}.getType();
            return gson.fromJson(json, listType);
        } catch (Exception e) {
            Log.e(TAG, "Error parsing notifications", e);
            return null;
        }
    }

    // ---------------------------------------------------------------------
    // LOGIN & REGISTER
    // ---------------------------------------------------------------------
    public ApiResponse login(String email, String password) {
        try {
            JSONObject data = new JSONObject();
            data.put("email", email);
            data.put("password", password);

            RequestBody body = RequestBody.create(data.toString(), JSON);
            Request request = new Request.Builder()
                    .url(BASE_URL + "/auth/login")
                    .addHeader("X-Client-Type", "Mobile")
                    .post(body)
                    .build();

            logRequest("POST", "/auth/login", data);

            Response response = client.newCall(request).execute();
            String responseBody = response.body() != null ? response.body().string() : "";
            logApi("POST", "/auth/login", response, responseBody);

            if (response.code() == 200 && !responseBody.isEmpty()) {
                JSONObject json = new JSONObject(responseBody);
                String token = json.optString("token", null);
                if (token != null) {
                    sessionManager.saveToken(token);
                    return new ApiResponse(true, "Login successful", token);
                }
            }

            return new ApiResponse(false, "Login failed", null);
        } catch (Exception e) {
            Log.e(TAG, "Login error", e);
            return new ApiResponse(false, "Network error", null);
        }
    }

    public ApiResponse register(String name, String email, String password) {
        try {
            JSONObject data = new JSONObject();
            data.put("name", name);
            data.put("email", email);
            data.put("password", password);

            logRequest("POST", "/owners/register", data);

            RequestBody body = RequestBody.create(data.toString(), JSON);
            Request request = new Request.Builder()
                    .url(BASE_URL + "/owners/register")
                    .addHeader("X-Client-Type", "Mobile")
                    .post(body)
                    .build();

            Response response = client.newCall(request).execute();
            String responseBody = response.body() != null ? response.body().string() : "";
            logApi("POST", "/owners/register", response, responseBody);

            if (response.isSuccessful()) {
                return new ApiResponse(true, "Registration successful", responseBody);
            } else {
                JSONObject err = new JSONObject(responseBody);
                return new ApiResponse(false, err.optString("message", "Registration failed"), null);
            }
        } catch (Exception e) {
            Log.e(TAG, "Registration error", e);
            return new ApiResponse(false, "Network or parsing error", null);
        }
    }

    // ---------------------------------------------------------------------
    // GENERIC REQUESTS
    // ---------------------------------------------------------------------
    public ApiResponse get(String endpoint) {
        try {
            Request.Builder builder = new Request.Builder()
                    .url(BASE_URL + endpoint)
                    .get()
                    .addHeader("X-Client-Type", "Mobile");
            addAuth(builder);

            Response response = client.newCall(builder.build()).execute();
            String responseBody = response.body() != null ? response.body().string() : "";
            logApi("GET", endpoint, response, responseBody);

            if (response.isSuccessful())
                return new ApiResponse(true, "Success", responseBody);
            else {
                JSONObject err = new JSONObject(responseBody);
                return new ApiResponse(false, err.optString("message", "Failed"), null);
            }
        } catch (Exception e) {
            Log.e(TAG, "GET request error", e);
            return new ApiResponse(false, "Network error", null);
        }
    }

    public ApiResponse post(String endpoint, JSONObject data) {
        try {
            logRequest("POST", endpoint, data);
            RequestBody body = RequestBody.create(data.toString(), JSON);
            Request.Builder builder = new Request.Builder()
                    .url(BASE_URL + endpoint)
                    .post(body)
                    .addHeader("X-Client-Type", "Mobile");
            addAuth(builder);

            Response response = client.newCall(builder.build()).execute();
            String responseBody = response.body() != null ? response.body().string() : "";
            logApi("POST", endpoint, response, responseBody);

            if (response.isSuccessful())
                return new ApiResponse(true, "Success", responseBody);
            else {
                JSONObject err = new JSONObject(responseBody);
                return new ApiResponse(false, err.optString("message", "Request failed"), null);
            }
        } catch (Exception e) {
            Log.e(TAG, "POST request error", e);
            return new ApiResponse(false, "Network error", null);
        }
    }

    public ApiResponse patch(String endpoint, JSONObject data) {
        try {
            if (data != null) logRequest("PATCH", endpoint, data);
            RequestBody body = data != null
                    ? RequestBody.create(data.toString(), JSON)
                    : RequestBody.create("", JSON);

            Request.Builder builder = new Request.Builder()
                    .url(BASE_URL + endpoint)
                    .patch(body)
                    .addHeader("X-Client-Type", "Mobile");
            addAuth(builder);

            Response response = client.newCall(builder.build()).execute();
            String responseBody = response.body() != null ? response.body().string() : "";
            logApi("PATCH", endpoint, response, responseBody);

            if (response.isSuccessful())
                return new ApiResponse(true, "Success", responseBody);
            else {
                if (responseBody.isEmpty()) return new ApiResponse(false, "Empty error body", null);
                JSONObject err = new JSONObject(responseBody);
                return new ApiResponse(false, err.optString("message", "Failed"), null);
            }
        } catch (Exception e) {
            Log.e(TAG, "PATCH request error", e);
            return new ApiResponse(false, "Network error", null);
        }
    }

    public ApiResponse put(String endpoint, JSONObject data) {
        try {
            logRequest("PUT", endpoint, data);
            RequestBody body = RequestBody.create(data.toString(), JSON);
            Request.Builder builder = new Request.Builder()
                    .url(BASE_URL + endpoint)
                    .put(body)
                    .addHeader("X-Client-Type", "Mobile");
            addAuth(builder);

            Response response = client.newCall(builder.build()).execute();
            String responseBody = response.body() != null ? response.body().string() : "";
            logApi("PUT", endpoint, response, responseBody);

            if (response.isSuccessful())
                return new ApiResponse(true, "Success", responseBody);
            else {
                JSONObject err = new JSONObject(responseBody);
                return new ApiResponse(false, err.optString("message", "Failed"), null);
            }
        } catch (Exception e) {
            Log.e(TAG, "PUT request error", e);
            return new ApiResponse(false, "Network error", null);
        }
    }

    public ApiResponse delete(String endpoint) {
        try {
            Request.Builder builder = new Request.Builder()
                    .url(BASE_URL + endpoint)
                    .delete()
                    .addHeader("X-Client-Type", "Mobile");
            addAuth(builder);

            Response response = client.newCall(builder.build()).execute();
            String responseBody = response.body() != null ? response.body().string() : "";
            logApi("DELETE", endpoint, response, responseBody);

            if (response.isSuccessful())
                return new ApiResponse(true, "Deleted", responseBody);
            else {
                JSONObject err = new JSONObject(responseBody);
                return new ApiResponse(false, err.optString("message", "Failed"), null);
            }
        } catch (Exception e) {
            Log.e(TAG, "DELETE request error", e);
            return new ApiResponse(false, "Network error", null);
        }
    }

    // ---------------------------------------------------------------------
    // OTHER API FUNCTIONS
    // ---------------------------------------------------------------------
    public ApiResponse getUser() { return get("/auth/me"); }
    public ApiResponse getBookingsByStation(String stationId) {
        if (stationId == null || stationId.isEmpty() || stationId.equals("string"))
            return new ApiResponse(false, "No station assigned", null);
        return get("/bookings/station/" + stationId);
    }
    public ApiResponse deactivateEvOwner(String nic) { return patch("/owners/" + nic + "/deactivate", null); }
    public ApiResponse requestReactivation(String nic) { return patch("/owners/" + nic + "/request-reactivation", null); }

    // ---------------------------------------------------------------------
    // LOGOUT
    // ---------------------------------------------------------------------
    public ApiResponse logout() {
        try { post("/auth/logout", new JSONObject()); } catch (Exception ignored) {}
        sessionManager.clearToken();
        return new ApiResponse(true, "Logged out", null);
    }

    public ApiResponse logoutAndForget() {
        try { post("/auth/logout", new JSONObject()); } catch (Exception ignored) {}
        sessionManager.clearAll();
        return new ApiResponse(true, "Logged out & credentials cleared", null);
    }

    // ---------------------------------------------------------------------
// JWT DECODER – Extract operator station info from token
// ---------------------------------------------------------------------
    private JSONObject decodeJwtPayload(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) return null;
            String payload = new String(android.util.Base64.decode(parts[1], android.util.Base64.URL_SAFE));
            return new JSONObject(payload);
        } catch (Exception e) {
            Log.e(TAG, "JWT decode failed", e);
            return null;
        }
    }


    // ---------------------------------------------------------------------
// USER PARSER (Universal for Owner, Operator, Admin)
// ---------------------------------------------------------------------
    public User parseLoggedOwner(String json) {
        if (json == null || json.isEmpty()) {
            Log.e(TAG, "Cannot parse user: empty JSON");
            return null;
        }

        try {
            JSONObject o = new JSONObject(json);

            // Basic info
            String id = o.optString("id", null);
            String fullName = o.optString("fullName", null);
            String email = o.optString("email", null);
            String role = o.optString("role", null);
            boolean isActive = o.optBoolean("isActive", false);
            String createdAt = o.optString("createdAt", null);

            // Start building user
            User user = new User();
            user.setUserId(id);
            user.setFullName(fullName);
            user.setEmail(email);
            user.setRole(role);
            user.setActive(isActive);
            user.setCreatedAt(createdAt);

            // ✅ Try to extract station info from JWT
            String token = sessionManager.getToken();
            JSONObject payload = decodeJwtPayload(token);
            if (payload != null) {
                user.setStationId(payload.optString("stationId", null));
                user.setStationName(payload.optString("stationName", null));
                user.setStationLocation(payload.optString("stationLocation", null));
            }

            Log.d(TAG, "✅ Parsed logged user: " + user.toString());
            return user;

        } catch (JSONException e) {
            Log.e(TAG, "Error parsing user JSON", e);
            return null;
        }
    }


    // ---------------------------------------------------------------------
    // 🔹 HELPERS: AUTH + LOGGING
    // ---------------------------------------------------------------------
    private void addAuth(Request.Builder builder) {
        String token = sessionManager.getToken();
        if (token != null) builder.addHeader("Authorization", "Bearer " + token);
    }

    private void logRequest(String method, String endpoint, JSONObject data) {
        Log.d(TAG, "─────────────────────────────────────────────");
        Log.d(TAG, "🚀 " + method + " " + BASE_URL + endpoint);
        Log.d(TAG, "🔑 Token: " + (sessionManager.getToken() != null ? "Present ✅" : "Missing ❌"));
        if (data != null)
            Log.d(TAG, "📤 Request Body:\n" + formatJson(data.toString()));
        else
            Log.d(TAG, "📤 Request Body: (empty)");
        Log.d(TAG, "─────────────────────────────────────────────");
    }

    private void logApi(String method, String endpoint, Response response, String responseBody) {
        int code = response != null ? response.code() : -1;
        Log.d(TAG, "📡 " + method + " " + BASE_URL + endpoint);
        Log.d(TAG, "📦 Status Code: " + code);
        if (responseBody != null && !responseBody.isEmpty())
            Log.d(TAG, "📨 Response Body:\n" + formatJson(responseBody));
        else
            Log.d(TAG, "📨 Response Body: (empty)");
        Log.d(TAG, "─────────────────────────────────────────────");
    }

    private String formatJson(String raw) {
        try {
            if (raw.trim().startsWith("{"))
                return new JSONObject(raw).toString(2);
            else if (raw.trim().startsWith("["))
                return new org.json.JSONArray(raw).toString(2);
            else return raw;
        } catch (Exception e) {
            return raw;
        }
    }
}
