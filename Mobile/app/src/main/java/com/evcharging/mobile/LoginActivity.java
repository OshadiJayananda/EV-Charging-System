package com.evcharging.mobile;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.evcharging.mobile.network.ApiClient;
import com.evcharging.mobile.network.ApiResponse;
import com.evcharging.mobile.session.SessionManager;
import com.evcharging.mobile.utils.JwtUtils;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "Login";

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegister;
    private ProgressBar progressBar;
    private ApiClient apiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
        progressBar = findViewById(R.id.progressBar);

        SessionManager sessionManager = new SessionManager(this);

        apiClient = new ApiClient(sessionManager);

        if (sessionManager.isLoggedIn()) {
            redirectToRoleHome(sessionManager.getToken());
            finish();
        }

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogin();
            }
        });

        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegistrationActivity.class));
            }
        });
    }

    private void attemptLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        boolean isValid = true;

        // Validation
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            isValid = false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email");
            etEmail.requestFocus();
            isValid = false;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            isValid = false;
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            isValid = false;
        }

        if (!isValid) {
            return;
        }

        new LoginTask().execute(email, password);
    }

    private class LoginTask extends AsyncTask<String, Void, ApiResponse> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Show loading state
            btnLogin.setEnabled(false);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected ApiResponse doInBackground(String... params) {
            String email = params[0];
            String password = params[1];
            return apiClient.login(email, password);
        }

        @Override
        protected void onPostExecute(ApiResponse response) {
            super.onPostExecute(response);

            // Hide loading state
            btnLogin.setEnabled(true);
            progressBar.setVisibility(View.GONE);

            if (response.isSuccess() && response.getData() != null) {
                Toast.makeText(LoginActivity.this, response.getMessage(), Toast.LENGTH_SHORT).show();
                redirectToRoleHome(response.getData());
                finish();
            } else {
                Toast.makeText(LoginActivity.this, response.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void navigateToHome() {
        startActivity(new Intent(LoginActivity.this, HomeActivity.class));
        finish();
    }

    private void redirectToRoleHome(String token) {
        String role = JwtUtils.getRoleFromToken(token);
        if ("owner".equalsIgnoreCase(role)) {
            startActivity(new Intent(this, OwnerHomeActivity.class));
        } else if ("operator".equalsIgnoreCase(role)) {
            startActivity(new Intent(this, OperatorHomeActivity.class));
        } else {
            Toast.makeText(this, "Unknown role!", Toast.LENGTH_SHORT).show();
        }
    }
}