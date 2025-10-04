package com.evcharging.mobile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.evcharging.mobile.db.OperatorRepository;
import com.evcharging.mobile.session.SessionManager;
import com.evcharging.mobile.network.ApiClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private ProgressBar progressBar;
    private SessionManager sessionManager;

    private final OkHttpClient httpClient = new OkHttpClient();
    private static final String BASE_URL = ApiClient.getBaseUrl();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progressBar);
        sessionManager = new SessionManager(this);

        btnLogin.setOnClickListener(v -> attemptLogin());
    }

    private void attemptLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("email", email);
            jsonBody.put("password", password);
        } catch (JSONException e) {
            Log.e("Login", "JSON build error", e);
        }

        RequestBody body = RequestBody.create(jsonBody.toString(), MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(BASE_URL + "/auth/login")
                .post(body)
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(LoginActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                });
                Log.e("Login", "API call failed", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                runOnUiThread(() -> progressBar.setVisibility(View.GONE));

                if (response.isSuccessful()) {
                    try {
                        String responseBody = response.body().string();
                        JSONObject json = new JSONObject(responseBody);
                        String token = json.optString("token", "");

                        if (token.isEmpty()) {
                            runOnUiThread(() ->
                                    Toast.makeText(LoginActivity.this, "Login failed: No token received", Toast.LENGTH_SHORT).show());
                            return;
                        }

                        // ✅ Decode JWT and extract operator info
                        JSONObject payload = decodeJWT(token);
                        Log.d("JWT_PAYLOAD", payload.toString(2));

                        String operatorId = payload.optString("nameid", "N/A");
                        String fullName = payload.optString("FullName", "Operator");
                        String email = payload.optString("email", "");
                        String role = payload.optString("role", "");
                        boolean isActive = true;

                        // ✅ Save session + operator
                        sessionManager.saveToken(token);
                        OperatorRepository operatorRepo = new OperatorRepository(LoginActivity.this);
                        operatorRepo.saveOperator(operatorId, fullName, email, "", "", isActive);

                        Log.d("DB_DEBUG", "Saved Operator ID: " + operatorId + ", name: " + fullName + ", role: " + role);

                        runOnUiThread(() -> {
                            Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginActivity.this, OperatorHomeActivity.class);
                            startActivity(intent);
                            finish();
                        });

                    } catch (Exception e) {
                        Log.e("Login", "Processing error", e);
                    }
                } else {
                    runOnUiThread(() ->
                            Toast.makeText(LoginActivity.this, "Invalid credentials", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    // ✅ Decode JWT Token (Base64 middle section)
    private JSONObject decodeJWT(String jwt) {
        try {
            String[] parts = jwt.split("\\.");
            if (parts.length < 2) return new JSONObject();

            String payload = parts[1];
            byte[] decodedBytes = Base64.decode(payload, Base64.URL_SAFE | Base64.NO_WRAP);
            String decodedString = new String(decodedBytes);
            return new JSONObject(decodedString);
        } catch (Exception e) {
            Log.e("JWT", "Failed to decode", e);
            return new JSONObject();
        }
    }
}
