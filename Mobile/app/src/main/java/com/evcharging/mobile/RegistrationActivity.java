package com.evcharging.mobile;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.evcharging.mobile.network.ApiClient;
import com.evcharging.mobile.network.ApiResponse;
import com.evcharging.mobile.service.OwnerService;
import com.evcharging.mobile.session.SessionManager;

public class RegistrationActivity extends AppCompatActivity {

    private EditText etFullName, etEmail, etPhone, etNIC, etPassword, etConfirmPassword;
    private ImageView ivTogglePassword, ivToggleConfirmPassword;
    private Button btnRegister;
    private ProgressBar progressBar;
    private TextView txtBackToLogin;
    private OwnerService ownerService;
    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etNIC = findViewById(R.id.etNIC);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        ivTogglePassword = findViewById(R.id.ivTogglePassword);
        ivToggleConfirmPassword = findViewById(R.id.ivToggleConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        progressBar = findViewById(R.id.progressBar);
        txtBackToLogin = findViewById(R.id.btnGoToLogin);

        // Toggle for main password
        ivTogglePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isPasswordVisible = !isPasswordVisible;
                if (isPasswordVisible) {
                    etPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    ivTogglePassword.setImageResource(R.drawable.ic_visibility);
                } else {
                    etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    ivTogglePassword.setImageResource(R.drawable.ic_visibility_off);
                }
                etPassword.setSelection(etPassword.getText().length());
            }
        });

        // Toggle for confirm password
        ivToggleConfirmPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isConfirmPasswordVisible = !isConfirmPasswordVisible;
                if (isConfirmPasswordVisible) {
                    etConfirmPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    ivToggleConfirmPassword.setImageResource(R.drawable.ic_visibility);
                } else {
                    etConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    ivToggleConfirmPassword.setImageResource(R.drawable.ic_visibility_off);
                }
                etConfirmPassword.setSelection(etConfirmPassword.getText().length());
            }
        });

        ownerService = new OwnerService();

        btnRegister.setOnClickListener(view -> attemptRegistration());

        txtBackToLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegistrationActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void attemptRegistration() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String nic = etNIC.getText().toString().trim();
        String password = etPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();

        boolean isValid = true;

        if (TextUtils.isEmpty(fullName)) {
            etFullName.setError("Full Name is required");
            etFullName.requestFocus();
            isValid = false;
        }

        if (TextUtils.isEmpty(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Valid Email is required");
            etEmail.requestFocus();
            isValid = false;
        }

        if (TextUtils.isEmpty(phone)) {
            etPhone.setError("Phone is required");
            etPhone.requestFocus();
            isValid = false;
        }

        if (TextUtils.isEmpty(nic)) {
            etNIC.setError("NIC is required");
            etNIC.requestFocus();
            isValid = false;
        }

        if (TextUtils.isEmpty(password) || password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            isValid = false;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            isValid = false;
        }

        if (!isValid) return;

        new RegistrationTask().execute(nic, fullName, email, phone, password);
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
            String nic = params[0];
            String fullName = params[1];
            String email = params[2];
            String phone = params[3];
            String password = params[4];

            // Call new API endpoint
            return ownerService.registerOwner(nic, fullName, email, phone, password);
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
                // Clear previous errors
                etNIC.setError(null);
                etFullName.setError(null);
                etEmail.setError(null);
                etPhone.setError(null);
                etPassword.setError(null);
                etConfirmPassword.setError(null);

                // Check if the response contains field-specific errors
                String message = response.getMessage();
                if (message != null && message.contains(":")) {
                    // Parse messages per field
                    String[] lines = message.split("\n");
                    for (String line : lines) {
                        String[] parts = line.split(":", 2);
                        if (parts.length == 2) {
                            String field = parts[0].trim();
                            String errorMsg = parts[1].trim();
                            switch (field.toLowerCase()) {
                                case "nic":
                                    etNIC.setError(errorMsg);
                                    etNIC.requestFocus();
                                    break;
                                case "fullname":
                                    etFullName.setError(errorMsg);
                                    etFullName.requestFocus();
                                    break;
                                case "email":
                                    etEmail.setError(errorMsg);
                                    etEmail.requestFocus();
                                    break;
                                case "phone":
                                    etPhone.setError(errorMsg);
                                    etPhone.requestFocus();
                                    break;
                                case "password":
                                    etPassword.setError(errorMsg);
                                    etPassword.requestFocus();
                                    break;
                            }
                        } else {
                            // Fallback: show as Toast
                            Toast.makeText(RegistrationActivity.this, line.trim(), Toast.LENGTH_LONG).show();
                        }
                    }
                } else {
                    // Generic error fallback
                    Toast.makeText(RegistrationActivity.this, message, Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}
