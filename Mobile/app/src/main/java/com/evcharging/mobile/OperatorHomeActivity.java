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
                                        JSONArray jsonArray = new JSONArray(response.getData());
                                        ArrayList<String> bookingsList = new ArrayList<>();

                                        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

                                        for (int i = 0; i < jsonArray.length(); i++) {
                                                JSONObject obj = jsonArray.getJSONObject(i);
                                                String date = obj.optString("date", "");
                                                String status = obj.optString("status", "");
                                                String ownerName = obj.optString("evOwnerName", "Unknown");
                                                String time = obj.optString("slotTime", "-");

                                                // Only show today's bookings
                                                if (date.contains(today)) {
                                                        bookingsList.add(time + " - " + ownerName + " - " + status);
                                                }
                                        }

                                        if (bookingsList.isEmpty()) {
                                                bookingsList.add("No bookings for today");
                                        }

                                        ArrayAdapter<String> adapter = new ArrayAdapter<>(OperatorHomeActivity.this,
                                                android.R.layout.simple_list_item_1, bookingsList);
                                        lvTodayReservations.setAdapter(adapter);

                                        Log.d("BOOKINGS", "Loaded " + bookingsList.size() + " bookings");
                                } catch (Exception e) {
                                        Log.e("BOOKINGS", "Error parsing bookings: " + e.getMessage());
                                        String[] msg = {"Error loading bookings"};
                                        lvTodayReservations.setAdapter(new ArrayAdapter<>(OperatorHomeActivity.this,
                                                android.R.layout.simple_list_item_1, msg));
                                }
                        }
                }.execute();
        }

}
