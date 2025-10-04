package com.evcharging.mobile;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.evcharging.mobile.LoginActivity;
import com.evcharging.mobile.R;
import com.evcharging.mobile.db.OperatorRepository;
import com.evcharging.mobile.session.SessionManager;

public class OperatorHomeActivity extends AppCompatActivity {

        private TextView tvWelcomeOperator, tvStationInfo, tvOperatorId;
        private ImageButton btnLogout;
        private ListView lvTodayReservations;
        private Button btnUpdateSlots, btnViewBookings, btnViewProfile, btnCancelBookings;

        private SessionManager sessionManager;
        private OperatorRepository operatorRepo;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_operator_home);

                sessionManager = new SessionManager(this);
                operatorRepo = new OperatorRepository(this);

                tvWelcomeOperator = findViewById(R.id.tvWelcomeOperator);
                tvStationInfo = findViewById(R.id.tvStationInfo);
                tvOperatorId = findViewById(R.id.tvOperatorId);
                btnLogout = findViewById(R.id.btnLogout);
                lvTodayReservations = findViewById(R.id.lvTodayReservations);
                btnUpdateSlots = findViewById(R.id.btnUpdateSlots);
                btnViewBookings = findViewById(R.id.btnViewBookings);
                btnViewProfile = findViewById(R.id.btnViewProfile);
                btnCancelBookings = findViewById(R.id.btnCancelBookings);

                loadOperatorData();
                setupButtons();
        }

        private void loadOperatorData() {
                Cursor cursor = operatorRepo.getOperator();
                if (cursor.moveToFirst()) {
                        String fullName = cursor.getString(cursor.getColumnIndexOrThrow("fullName"));
                        String stationName = cursor.getString(cursor.getColumnIndexOrThrow("stationName"));
                        String operatorId = cursor.getString(cursor.getColumnIndexOrThrow("id"));

                        tvWelcomeOperator.setText("Welcome, " + fullName + "!");
                        tvStationInfo.setText("Station: " + stationName);
                        tvOperatorId.setText("Operator ID: " + operatorId);
                } else {
                        Toast.makeText(this, "No operator data found in local DB", Toast.LENGTH_SHORT).show();
                }
                cursor.close();
        }

        private void setupButtons() {
                btnLogout.setOnClickListener(v -> {
                        sessionManager.clearAll();
                        operatorRepo.clearOperator();

                        Intent intent = new Intent(this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                });

                btnUpdateSlots.setOnClickListener(v ->
                        Toast.makeText(this, "Update Slot Availability clicked", Toast.LENGTH_SHORT).show());

                btnViewBookings.setOnClickListener(v ->
                        Toast.makeText(this, "View All Bookings clicked", Toast.LENGTH_SHORT).show());

                btnViewProfile.setOnClickListener(v ->
                        Toast.makeText(this, "My Profile clicked", Toast.LENGTH_SHORT).show());

                btnCancelBookings.setOnClickListener(v ->
                        Toast.makeText(this, "Cancel Bookings clicked", Toast.LENGTH_SHORT).show());
        }
}
