package com.evcharging.mobile;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.evcharging.mobile.LoginActivity;
import com.evcharging.mobile.R;
import com.evcharging.mobile.model.User;
import com.evcharging.mobile.network.ApiClient;
import com.evcharging.mobile.network.ApiResponse;
import com.evcharging.mobile.session.SessionManager;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;

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

        tvWelcomeOperator.setText("Welcome, " + user.getFullName());
        tvOperatorId.setText("Operator ID: " + user.getUserId());

        String stationName = (user.getStationName() != null && !user.getStationName().equals("string"))
                ? user.getStationName()
                : "Pending";
        tvStationInfo.setText("Station: " + stationName);

        loadTodayBookings();
    }

    private void setupClicks() {
        ivProfile.setOnClickListener(v -> startActivity(new Intent(this, OperatorProfileActivity.class)));
        btnViewProfile.setOnClickListener(v -> startActivity(new Intent(this, OperatorProfileActivity.class)));

        btnLogout.setOnClickListener(v -> {
            ApiClient apiClient = new ApiClient(session);
            ApiResponse response = apiClient.logout();
            Toast.makeText(this, response.getMessage(), Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
        });

        btnUpdateSlots = findViewById(R.id.btnUpdateSlots);
        btnUpdateSlots.setOnClickListener(v -> {
            Intent intent = new Intent(this, OperatorUpdateSlotsActivity.class);
            startActivity(intent);
        });

        btnViewBookings.setOnClickListener(v -> startActivity(new Intent(this, AllBookingsActivity.class)));

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
                return apiClient.get("/bookings/station/" + user.getStationId() + "/today");
            }

            @Override
            protected void onPostExecute(ApiResponse response) {
                if (response == null || !response.isSuccess() || response.getData() == null) {
                    String[] msg = {"No bookings found for today"};
                    lvTodayReservations.setAdapter(
                            new ArrayAdapter<>(OperatorHomeActivity.this,
                                    android.R.layout.simple_list_item_1, msg));
                    return;
                }

                try {
                    JSONArray jsonArray = new JSONArray(response.getData());
                    ArrayList<JSONObject> reservations = new ArrayList<>();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject obj = jsonArray.getJSONObject(i);
                        if (obj.optString("status").equalsIgnoreCase("Approved")) {
                            reservations.add(obj);
                        }
                    }

                    if (reservations.isEmpty()) {
                        String[] msg = {"No approved reservations today"};
                        lvTodayReservations.setAdapter(
                                new ArrayAdapter<>(OperatorHomeActivity.this,
                                        android.R.layout.simple_list_item_1, msg));
                        return;
                    }

                    TodayReservationAdapter adapter =
                            new TodayReservationAdapter(OperatorHomeActivity.this, reservations);
                    lvTodayReservations.setAdapter(adapter);

                    lvTodayReservations.setOnItemClickListener((parent, view, position, id) -> {
                        JSONObject obj = reservations.get(position);
                        Intent intent = new Intent(OperatorHomeActivity.this, BookingDetailsActivity.class);
                        intent.putExtra("bookingId", obj.optString("bookingId"));
                        intent.putExtra("status", obj.optString("status"));
                        intent.putExtra("startTime", obj.optString("formattedStartTime", obj.optString("startTime")));
                        intent.putExtra("endTime", obj.optString("formattedEndTime", obj.optString("endTime")));
                        intent.putExtra("qrImageBase64", obj.optString("qrImageBase64"));
                        intent.putExtra("qrCode", obj.optString("qrCode"));
                        startActivity(intent);
                    });

                } catch (Exception e) {
                    Log.e("BOOKINGS", "Error parsing bookings: " + e.getMessage());
                }
            }

        }.execute();
    }
}
