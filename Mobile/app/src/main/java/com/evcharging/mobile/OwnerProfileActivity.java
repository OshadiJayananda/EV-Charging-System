package com.evcharging.mobile;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.evcharging.mobile.model.User;
import com.evcharging.mobile.network.ApiClient;
import com.evcharging.mobile.network.ApiResponse;
import com.evcharging.mobile.session.SessionManager;

public class OwnerProfileActivity extends AppCompatActivity {

    private TextView tvName, tvEmail, tvNic, tvAccountStatus;
    private ImageView ivProfilePic;
    private Button btnEditProfile, btnDeactivate, btnRequestReactivation;
    private ImageButton btnBack;
    private SessionManager sessionManager;
    private ApiClient apiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_profile);

        sessionManager = new SessionManager(this);
        apiClient = new ApiClient(sessionManager);

        tvName = findViewById(R.id.tvOwnerName);
        tvEmail = findViewById(R.id.tvOwnerEmail);
        tvNic = findViewById(R.id.tvOwnerNic);
        tvAccountStatus = findViewById(R.id.tvAccountStatus);
        ivProfilePic = findViewById(R.id.ivProfilePic);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnDeactivate = findViewById(R.id.btnDeactivate);
        btnRequestReactivation = findViewById(R.id.btnRequestReactivation);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());
        btnEditProfile.setOnClickListener(v ->
                startActivity(new Intent(this, OwnerEditProfileActivity.class)));

        showLocalUserProfile();

        // Load profile from API
        new LoadProfileTask().execute();

        // Deactivate button
        btnDeactivate.setOnClickListener(v ->
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Deactivate Account")
                        .setMessage("Are you sure you want to deactivate your account?")
                        .setPositiveButton("Yes", (dialog, which) -> new DeactivateTask().execute())
                        .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                        .show()
        );

        // Reactivation button
        btnRequestReactivation.setOnClickListener(v ->
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Request Reactivation")
                        .setMessage("Do you want to request reactivation of your account?")
                        .setPositiveButton("Yes", (dialog, which) -> new ReactivationTask().execute())
                        .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                        .show()
        );
    }

    /**
     * Load user profile from API
     */
    private class LoadProfileTask extends AsyncTask<Void, Void, User> {
        @Override
        protected User doInBackground(Void... voids) {
            ApiResponse response = apiClient.getUser();
            if (response.isSuccess() && response.getData() != null) {
                // Parse JSON into User object
                return apiClient.parseLoggedOwner(response.getData());
            }
            return null;
        }

        @Override
        protected void onPostExecute(User user) {
            if (user != null) {
                // Save updated user to session
                sessionManager.saveLoggedInUser(user);

                // Update UI
                tvName.setText(user.getFullName() != null ? user.getFullName() : "N/A");
                tvEmail.setText(user.getEmail() != null ? user.getEmail() : "N/A");
                tvNic.setText(user.getUserId() != null ? "NIC: " + user.getUserId() : "NIC: N/A");

                if (user.isActive()) {
                    tvAccountStatus.setText("Active");
                    tvAccountStatus.setTextColor(getResources().getColor(R.color.green));
                    btnDeactivate.setVisibility(View.VISIBLE);
                    btnRequestReactivation.setVisibility(View.GONE);
                } else {
                    tvAccountStatus.setText("Deactivated");
                    tvAccountStatus.setTextColor(getResources().getColor(R.color.red));
                    btnDeactivate.setVisibility(View.GONE);
                    btnRequestReactivation.setVisibility(View.VISIBLE);
                }
            } else {
                Toast.makeText(OwnerProfileActivity.this,
                        "Failed to load profile", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String getOwnerNic() {
        User loggedInUser = sessionManager.getLoggedInUser();
        return (loggedInUser != null && loggedInUser.getUserId() != null)
                ? loggedInUser.getUserId().trim()
                : "";
    }

    private class DeactivateTask extends AsyncTask<Void, Void, ApiResponse> {
        private final String nic = getOwnerNic();

        @Override
        protected ApiResponse doInBackground(Void... voids) {
            return apiClient.deactivateEvOwner(nic);
        }

        @Override
        protected void onPostExecute(ApiResponse response) {
            Toast.makeText(OwnerProfileActivity.this, response.getMessage(), Toast.LENGTH_SHORT).show();
            if (response.isSuccess()) {
                new LoadProfileTask().execute(); // Refresh profile after deactivation
            }
        }
    }

    private class ReactivationTask extends AsyncTask<Void, Void, ApiResponse> {
        private final String nic;

        public ReactivationTask() {
            this.nic = getOwnerNic();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (nic == null || nic.isEmpty()) {
                Toast.makeText(OwnerProfileActivity.this, "NIC is empty!", Toast.LENGTH_SHORT).show();
                cancel(true); // Stop the AsyncTask from executing
            }
        }

        @Override
        protected ApiResponse doInBackground(Void... voids) {
            if (isCancelled()) return null; // In case task was cancelled in onPreExecute
            return apiClient.requestReactivation(nic);
        }

        @Override
        protected void onPostExecute(ApiResponse response) {
            if (response == null) return; // Task was cancelled
            Toast.makeText(OwnerProfileActivity.this, response.getMessage(), Toast.LENGTH_SHORT).show();
            if (response.isSuccess()) {
                new LoadProfileTask().execute(); // Refresh profile after reactivation
            }
        }
    }

    private void showLocalUserProfile() {
        User localUser = sessionManager.getLoggedInUser();
        if (localUser != null) {
            tvName.setText(localUser.getFullName() != null ? localUser.getFullName() : "N/A");
            tvEmail.setText(localUser.getEmail() != null ? localUser.getEmail() : "N/A");
            tvNic.setText(localUser.getUserId() != null ? "NIC: " + localUser.getUserId() : "NIC: N/A");

            if (localUser.isActive()) {
                tvAccountStatus.setText("Active");
                tvAccountStatus.setTextColor(getResources().getColor(R.color.green));
                btnDeactivate.setVisibility(View.VISIBLE);
                btnRequestReactivation.setVisibility(View.GONE);
            } else {
                tvAccountStatus.setText("Deactivated");
                tvAccountStatus.setTextColor(getResources().getColor(R.color.red));
                btnDeactivate.setVisibility(View.GONE);
                btnRequestReactivation.setVisibility(View.VISIBLE);
            }
        }
    }
}
