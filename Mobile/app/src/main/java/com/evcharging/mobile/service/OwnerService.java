package com.evcharging.mobile.service;

import static com.evcharging.mobile.network.ApiClient.getApiBaseUrl;

import android.util.Log;

import com.evcharging.mobile.network.ApiResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OwnerService {

    private static final String TAG = "OwnerService";
    private static final okhttp3.MediaType JSON = okhttp3.MediaType.get("application/json; charset=utf-8");

    private OkHttpClient client;

    public OwnerService() {
        // Initialize OkHttpClient (unsafe SSL can be added if needed for ngrok/dev)
        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    public ApiResponse registerOwner(String nic, String fullName, String email, String phone, String password) {
        try {
            // Build JSON body
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("nic", nic);
            jsonBody.put("fullName", fullName);
            jsonBody.put("email", email);
            jsonBody.put("phone", phone);
            jsonBody.put("password", password);

            RequestBody body = RequestBody.create(jsonBody.toString(), JSON);

            Request request = new Request.Builder()
                    .url(getApiBaseUrl() + "/owners/register")
                    .post(body)
                    .build();

            Response response = client.newCall(request).execute();

            String responseBody = response.body() != null ? response.body().string() : "";
            Log.d(TAG, "RegisterOwner Response: " + responseBody);

            if (response.isSuccessful()) {
                JSONObject jsonResponse = new JSONObject(responseBody);
                String message = jsonResponse.optString("message", "Registration successful");
                return new ApiResponse(true, message, null);
            } else {
                // Parse backend message even for errors
                JSONObject jsonResponse = new JSONObject(responseBody);
                String message = jsonResponse.optString("message", "Registration failed");

                // Optional: parse detailed validation errors if present
                if (jsonResponse.has("errors")) {
                    JSONObject errors = jsonResponse.getJSONObject("errors");
                    StringBuilder errorsMessage = new StringBuilder();
                    for (Iterator<String> it = errors.keys(); it.hasNext(); ) {
                        String key = it.next();
                        for (int i = 0; i < errors.getJSONArray(key).length(); i++) {
                            errorsMessage.append(errors.getJSONArray(key).getString(i));
                            if (i < errors.getJSONArray(key).length() - 1) errorsMessage.append(" ");
                        }
                        errorsMessage.append("\n");
                    }
                    message = errorsMessage.toString().trim();
                }

                return new ApiResponse(false, message, null);
            }

        } catch (IOException | JSONException e) {
            Log.e(TAG, "Error during owner registration", e);
            return new ApiResponse(false, "Network or parsing error: " + e.getMessage(), null);
        }
    }
}
