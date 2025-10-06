package com.evcharging.mobile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.evcharging.mobile.LoginActivity;
import com.evcharging.mobile.R;
import com.evcharging.mobile.model.User;
import com.evcharging.mobile.network.ApiClient;
import com.evcharging.mobile.network.ApiResponse;
import com.evcharging.mobile.session.SessionManager;

import android.os.AsyncTask;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class OperatorHomeActivity extends AppCompatActivity {

        private SessionManager session;
        private ImageView ivProfile;
        private TextView tvWelcomeOperator, tvStationInfo, tvOperatorId;
        private Button btnViewProfile, btnUpdateSlots, btnViewBookings, btnCancelBookings;
        private ImageButton btnLogout;
        private ListView lvTodayReservations;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_operator_home);
                setTitle("Operator Dashboard");

                session = new SessionManager(this);
                bindViews();
                loadOperatorData();
                setupClicks();
        }

        private void bindViews() {
                ivProfile = findViewById(R.id.ivProfile);
                tvWelcomeOperator = findViewById(R.id.tvWelcomeOperator);
                tvStationInfo = findViewById(R.id.tvStationInfo);
                tvOperatorId = findViewById(R.id.tvOperatorId);
                btnViewProfile = findViewById(R.id.btnViewProfile);
                btnUpdateSlots = findViewById(R.id.btnUpdateSlots);
                btnViewBookings = findViewById(R.id.btnViewBookings);
                btnCancelBookings = findViewById(R.id.btnCancelBookings);
                btnLogout = findViewById(R.id.btnLogout);
                lvTodayReservations = findViewById(R.id.lvTodayReservations);
        }

        private void loadOperatorData() {
                User user = session.getLoggedInUser();
                if (user == null) {
                        Toast.makeText(this, "Session expired, please log in again.", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, LoginActivity.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                        finish();
                        return;
                }

                // Basic Info
                tvWelcomeOperator.setText("Welcome, " + user.getFullName());
                tvOperatorId.setText("Operator ID: " + user.getUserId());

                // Station Info
                String stationName = (user.getStationName() != null && !user.getStationName().equals("string"))
                        ? user.getStationName()
                        : "Pending";
                String stationId = (user.getStationId() != null && !user.getStationId().equals("string"))
                        ? user.getStationId()
                        : "Pending";
                tvStationInfo.setText("Station: " + stationName );

                // Load real data
                loadTodayBookings();

        }

        private void setupClicks() {
                ivProfile.setOnClickListener(v -> startActivity(new Intent(this, com.evcharging.mobile.OperatorProfileActivity.class)));
                btnViewProfile.setOnClickListener(v -> startActivity(new Intent(this, com.evcharging.mobile.OperatorProfileActivity.class)));

                btnLogout.setOnClickListener(v -> {
                        ApiClient apiClient = new ApiClient(session);
                        ApiResponse response = apiClient.logout();
                        Toast.makeText(this, response.getMessage(), Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, LoginActivity.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                        finish();
                });

                // Placeholder actions
                btnUpdateSlots.setOnClickListener(v -> Toast.makeText(this, "Manage Slots (coming soon)", Toast.LENGTH_SHORT).show());
                btnViewBookings.setOnClickListener(v -> Toast.makeText(this, "View Bookings (coming soon)", Toast.LENGTH_SHORT).show());
                btnCancelBookings.setOnClickListener(v -> Toast.makeText(this, "Cancel Bookings (coming soon)", Toast.LENGTH_SHORT).show());
        }

        private void loadTodayBookings() {
                User user = session.getLoggedInUser();

                if (user == null || user.getStationId() == null || user.getStationId().equals("string")) {
                        String[] msg = {"No station assigned yet"};
                        lvTodayReservations.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, msg));
                        return;
                }

                new AsyncTask<Void, Void, ApiResponse>() {
                        @Override
                        protected ApiResponse doInBackground(Void... voids) {
                                ApiClient apiClient = new ApiClient(session);
                                return apiClient.getBookingsByStation(user.getStationId());
                        }

                        @Override
                        protected void onPostExecute(ApiResponse response) {
                                if (!response.isSuccess() || response.getData() == null) {
                                        String[] msg = {"No bookings found for today"};
                                        lvTodayReservations.setAdapter(new ArrayAdapter<>(OperatorHomeActivity.this,
                                                android.R.layout.simple_list_item_1, msg));
                                        return;
                                }

                                try {
                                        Log.d("BOOKINGS", "Raw response data: " + response.getData());

                                        JSONArray jsonArray = new JSONArray(response.getData());
                                        Log.d("BOOKINGS", "JSONArray length: " + jsonArray.length());

                                        ArrayList<String> bookingsList = new ArrayList<>();

                                        for (int i = 0; i < jsonArray.length(); i++) {
                                                JSONObject obj = jsonArray.getJSONObject(i);
                                                Log.d("BOOKINGS", "Parsed booking " + (i + 1) + ": " + obj.toString());

                                                String id = obj.optString("bookingId", "N/A");
                                                String status = obj.optString("status", "N/A");
                                                String startTime = obj.optString("startTime", "N/A");
                                                String endTime = obj.optString("endTime", "N/A");

                                                bookingsList.add("ID: " + id + "\n" +
                                                        "Start: " + startTime + "\n" +
                                                        "End: " + endTime + "\n" +
                                                        "Status: " + status);
                                        }

                                        Log.d("BOOKINGS", "Final list size: " + bookingsList.size());

                                        if (bookingsList.isEmpty()) {
                                                bookingsList.add("No bookings found for this station");
                                        }

                                        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                                OperatorHomeActivity.this,
                                                android.R.layout.simple_list_item_1,
                                                bookingsList
                                        );
                                        lvTodayReservations.setAdapter(adapter);

                                        } catch (Exception e) {
                                        Log.e("BOOKINGS", "Error parsing bookings: " + e.getMessage());
                                        }

                        }
                }.execute();
        }

}
