package com.evcharging.mobile;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
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

public class RegistrationActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPassword, etConfirmPassword;
    private Button btnRegister;
    private ProgressBar progressBar;
    private TextView txtBackToLogin;
    private ApiClient apiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        progressBar = findViewById(R.id.progressBar);

        txtBackToLogin = findViewById(R.id.btnGoToLogin);

        SessionManager sessionManager = new SessionManager(this);
        apiClient = new ApiClient(sessionManager);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptRegistration();
            }
        });

        txtBackToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate back to LoginActivity
                startActivity(new Intent(RegistrationActivity.this, LoginActivity.class));
                finish();
            }
        });
    }

    private void attemptRegistration() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();

        boolean isValid = true;

        if (TextUtils.isEmpty(name)) {
            etName.setError("Name is required");
            etName.requestFocus();
            isValid = false;
        }

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email");
            etEmail.requestFocus();
            isValid = false;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            isValid = false;
        } else if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            isValid = false;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            isValid = false;
        }

        if (!isValid) {
            return;
        }

        // Execute registration task
        new RegistrationTask().execute(name, email, password);
    }

    private class RegistrationTask extends AsyncTask<String, Void, ApiResponse> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            btnRegister.setEnabled(false);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected ApiResponse doInBackground(String... params) {
            String name = params[0];
            String email = params[1];
            String password = params[2];
            return apiClient.register(name, email, password);
        }

        @Override
        protected void onPostExecute(ApiResponse response) {
            super.onPostExecute(response);
            btnRegister.setEnabled(true);
            progressBar.setVisibility(View.GONE);

            if (response.isSuccess()) {
                Toast.makeText(RegistrationActivity.this, response.getMessage(), Toast.LENGTH_SHORT).show();
                startActivity(new Intent(RegistrationActivity.this, LoginActivity.class));
                finish();
            } else {
                Toast.makeText(RegistrationActivity.this, response.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }
}
