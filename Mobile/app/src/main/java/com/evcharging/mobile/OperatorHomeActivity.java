package com.evcharging.mobile;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.evcharging.mobile.db.BookingRepository;
import com.evcharging.mobile.db.OperatorRepository;
import com.evcharging.mobile.network.ApiClient;
import com.evcharging.mobile.session.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.*;

public class OperatorHomeActivity extends AppCompatActivity {

        private TextView tvWelcomeOperator, tvStationInfo, tvOperatorId;
        private ImageButton btnLogout;
        private ListView lvTodayReservations;
        private Button btnUpdateSlots, btnViewBookings, btnViewProfile, btnCancelBookings;

        private SessionManager sessionManager;
        private OperatorRepository operatorRepo;
        private BookingRepository bookingRepo;

        private final OkHttpClient httpClient = new OkHttpClient();
        private static final String BASE_URL = ApiClient.getBaseUrl();

        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_operator_home);

                sessionManager = new SessionManager(this);
                operatorRepo = new OperatorRepository(this);
                bookingRepo = new BookingRepository(this);

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
                fetchTodayBookings(); // Start fetching bookings
        }

        private void loadOperatorData() {
                Cursor cursor = operatorRepo.getOperator();
                if (cursor != null && cursor.moveToFirst()) {
                        String fullName = cursor.getString(cursor.getColumnIndexOrThrow("fullName"));
                        String stationName = cursor.getString(cursor.getColumnIndexOrThrow("stationName"));
                        String operatorId = cursor.getString(cursor.getColumnIndexOrThrow("id"));

                        tvWelcomeOperator.setText("Welcome, " + fullName + "!");
                        tvStationInfo.setText("Station: " + stationName);
                        tvOperatorId.setText("Operator ID: " + operatorId);
                } else {
                        Toast.makeText(this, "No operator data found in local DB", Toast.LENGTH_SHORT).show();
                }
                if (cursor != null) {
                        cursor.close();
                }
        }

        private void setupButtons() {
                btnLogout.setOnClickListener(v -> {
                        sessionManager.clearAll();
                        operatorRepo.clearOperator();
                        bookingRepo.clearBookings();
                        Intent intent = new Intent(this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                });
        }

        private void fetchTodayBookings() {
                Cursor cursor = operatorRepo.getOperator();
                String stationId = "";
                if (cursor.moveToFirst()) {
                        stationId = cursor.getString(cursor.getColumnIndexOrThrow("stationId"));
                }
                cursor.close();

                if (stationId.isEmpty()) {
                        Toast.makeText(this, "No Station ID found for operator", Toast.LENGTH_SHORT).show();
                        return;
                }

                String url = BASE_URL + "/bookings/station/" + stationId;
                Log.d("BOOKING_API", "Fetching: " + url);

                Request request = new Request.Builder()
                        .url(url)
                        .addHeader("Authorization", "Bearer " + sessionManager.getToken())
                        .get()
                        .build();

                httpClient.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                                runOnUiThread(() ->
                                        Toast.makeText(OperatorHomeActivity.this, "Network error", Toast.LENGTH_SHORT).show());
                                Log.e("BOOKING_API", "API failure", e);
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                                if (response.isSuccessful()) {
                                        String responseBody = response.body().string();
                                        JSONArray arr = null;
                                        try {
                                                arr = new JSONArray(responseBody);
                                        } catch (JSONException e) {
                                                throw new RuntimeException(e);
                                        }

                                        // Log the response to verify data
                                        Log.d("BOOKING_API_RESPONSE", responseBody);

                                        // Save bookings
                                        bookingRepo.clearBookings();
                                        bookingRepo.saveBookings(arr);
                                        runOnUiThread(() -> showBookings());
                                } else {
                                        Log.e("BOOKING_API", "Error fetching bookings: " + response.code());
                                }
                        }
                });
        }

        private void showBookings() {
                Cursor cursor = bookingRepo.getAllBookings();

                // If no data is found in the local DB, display a toast
                if (cursor.getCount() == 0) {
                        Toast.makeText(this, "No bookings found for today", Toast.LENGTH_SHORT).show();
                        lvTodayReservations.setAdapter(null); // Clear the ListView
                        return;
                }

                // Otherwise, show the bookings in the ListView
                SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                        this,
                        R.layout.booking_list_item,
                        cursor,
                        new String[]{"_id", "ownerId", "status", "startTime"},
                        new int[]{R.id.tvBookingId, R.id.tvOwnerId, R.id.tvStatus, R.id.tvTime},
                        0
                );
                lvTodayReservations.setAdapter(adapter);
        }
}
