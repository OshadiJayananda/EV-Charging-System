package com.evcharging.mobile;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.evcharging.mobile.network.ApiClient;
import com.evcharging.mobile.network.ApiResponse;
import com.evcharging.mobile.session.SessionManager;

public class OwnerProfileActivity extends AppCompatActivity {

    private TextView tvName, tvEmail, tvNic;
    private ImageView ivProfilePic;
    private Button btnEditProfile, btnDeactivate, btnRequestReactivation;
    private ImageButton btnBack;
    private SessionManager sessionManager;
    private ApiClient apiClient;
    private String nic = "991234567V";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_profile);

        sessionManager = new SessionManager(this);
        apiClient = new ApiClient(sessionManager);

        tvName = findViewById(R.id.tvOwnerName);
        tvEmail = findViewById(R.id.tvOwnerEmail);
        tvNic = findViewById(R.id.tvOwnerNic);
        ivProfilePic = findViewById(R.id.ivProfilePic);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnDeactivate = findViewById(R.id.btnDeactivate);

        btnRequestReactivation = findViewById(R.id.btnRequestReactivation);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        btnEditProfile.setOnClickListener(v ->
                startActivity(new Intent(this, OwnerEditProfileActivity.class))
        );

        btnDeactivate.setOnClickListener(v -> new DeactivateTask().execute());
        btnRequestReactivation.setOnClickListener(v -> new ReactivationTask().execute());
    }

    private class DeactivateTask extends AsyncTask<Void, Void, ApiResponse> {
        @Override
        protected ApiResponse doInBackground(Void... voids) {
            return apiClient.deactivateEvOwner(nic);
        }

        @Override
        protected void onPostExecute(ApiResponse response) {
            Toast.makeText(OwnerProfileActivity.this, response.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private class ReactivationTask extends AsyncTask<Void, Void, ApiResponse> {
        @Override
        protected ApiResponse doInBackground(Void... voids) {
            return apiClient.requestReactivation(nic);
        }

        @Override
        protected void onPostExecute(ApiResponse response) {
            Toast.makeText(OwnerProfileActivity.this, response.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
