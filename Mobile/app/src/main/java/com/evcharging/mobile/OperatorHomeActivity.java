package com.evcharging.mobile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.evcharging.mobile.network.ApiClient;
import com.evcharging.mobile.network.ApiResponse;
import com.evcharging.mobile.session.SessionManager;

public class OperatorHomeActivity extends AppCompatActivity {

        Button btnUpdateSlots, btnViewBookings, btnCancelBookings;
        private ImageView ivProfile;
        private TextView tvWelcomeOperator, tvStationInfo, tvOperatorId;
        private Button btnViewProfile;
        private ImageButton btnLogout;
        ListView lvTodayReservations;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_operator_home);

                setTitle("Operator Dashboard");

                initializeViews();

                // Hardcoded sample reservations for today
                String[] reservations = {
                                "10:00 AM - EV1234 - Station A",
                                "11:30 AM - EV5678 - Station B",
                                "01:00 PM - EV9012 - Station C",
                                "03:30 PM - EV3456 - Station A"
                };

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                this,
                                android.R.layout.simple_list_item_1,
                                reservations);

                lvTodayReservations.setAdapter(adapter);

                setupClickListeners();
        }

        private void setupClickListeners() {
                // Profile Image Click
                ivProfile.setOnClickListener(v -> navigateToProfile());

                // View Profile Button Click
                btnViewProfile.setOnClickListener(v -> navigateToProfile());

                // Logout Button
                btnLogout.setOnClickListener(v -> attemptLogout());

                // Other buttons
                btnUpdateSlots.setOnClickListener(v -> Toast
                                .makeText(OperatorHomeActivity.this, "Update Slots Clicked", Toast.LENGTH_SHORT)
                                .show());

                btnViewBookings.setOnClickListener(v -> Toast
                                .makeText(OperatorHomeActivity.this, "View Bookings Clicked", Toast.LENGTH_SHORT)
                                .show());

                btnCancelBookings.setOnClickListener(v -> Toast
                                .makeText(OperatorHomeActivity.this, "Cancel Bookings Clicked", Toast.LENGTH_SHORT)
                                .show());
        }

        private void attemptLogout() {
                ApiClient apiClient = new ApiClient(new SessionManager(this));
                ApiResponse response = apiClient.logout();

                Toast.makeText(
                                OperatorHomeActivity.this,
                                response.getMessage(),
                                Toast.LENGTH_SHORT).show();

                // Redirect to login screen after logout
                Intent intent = new Intent(OperatorHomeActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear backstack
                startActivity(intent);
                finish();
        }

        private void initializeViews() {
                btnUpdateSlots = findViewById(R.id.btnUpdateSlots);
                lvTodayReservations = findViewById(R.id.lvTodayReservations);
                btnViewBookings = findViewById(R.id.btnViewBookings);
                btnCancelBookings = findViewById(R.id.btnCancelBookings);

                ivProfile = findViewById(R.id.ivProfile);
                tvWelcomeOperator = findViewById(R.id.tvWelcomeOperator);
                tvStationInfo = findViewById(R.id.tvStationInfo);
                tvOperatorId = findViewById(R.id.tvOperatorId);
                btnViewProfile = findViewById(R.id.btnViewProfile);
                btnLogout = findViewById(R.id.btnLogout);
        }

        private void navigateToProfile() {
                Toast.makeText(OperatorHomeActivity.this, "Profile Clicked", Toast.LENGTH_SHORT)
                        .show();
        }
}
