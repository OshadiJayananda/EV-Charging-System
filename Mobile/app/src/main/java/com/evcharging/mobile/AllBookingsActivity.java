package com.evcharging.mobile;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.evcharging.mobile.model.User;
import com.evcharging.mobile.network.ApiClient;
import com.evcharging.mobile.network.ApiResponse;
import com.evcharging.mobile.session.SessionManager;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;

public class AllBookingsActivity extends AppCompatActivity {

    private SessionManager session;
    private ListView lvAllBookings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_bookings);
        setTitle("All Upcoming Bookings");

        session = new SessionManager(this);
        lvAllBookings = findViewById(R.id.lvAllBookings);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        loadUpcomingBookings();
    }

private void loadUpcomingBookings() {
    new AsyncTask<Void, Void, ApiResponse>() {
        @Override
        protected ApiResponse doInBackground(Void... voids) {
            User user = session.getLoggedInUser();
            if (user == null || user.getStationId() == null) {
                return new ApiResponse(false, "No station assigned", null);
            }

            ApiClient apiClient = new ApiClient(session);
            return apiClient.get("/bookings/station/" + user.getStationId() + "/upcoming");
        }


        @Override
        protected void onPostExecute(ApiResponse response) {
            if (response == null || !response.isSuccess() || response.getData() == null) {
                Toast.makeText(AllBookingsActivity.this, "No upcoming bookings found", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                JSONArray jsonArray = new JSONArray(response.getData());
                ArrayList<JSONObject> bookings = new ArrayList<>();

                for (int i = 0; i < jsonArray.length(); i++) {
                    bookings.add(jsonArray.getJSONObject(i));
                }

                if (bookings.isEmpty()) {
                    Toast.makeText(AllBookingsActivity.this, "No upcoming bookings found for this station", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Use custom adapter for modern card design
                BookingAdapter adapter = new BookingAdapter(AllBookingsActivity.this, bookings);
                lvAllBookings.setAdapter(adapter);

                // (Optional) Click event → Open BookingDetailsActivity
                lvAllBookings.setOnItemClickListener((parent, view, position, id) -> {
                    JSONObject obj = bookings.get(position);
                    Intent intent = new Intent(AllBookingsActivity.this, BookingDetailsActivity.class);
                    intent.putExtra("bookingId", obj.optString("bookingId"));
                    intent.putExtra("status", obj.optString("status"));
                    intent.putExtra("formattedStartTime", obj.optString("formattedStartTime"));
                    intent.putExtra("formattedEndTime", obj.optString("formattedEndTime"));
                    intent.putExtra("qrImageBase64", obj.optString("qrImageBase64"));
                    startActivity(intent);
                });

            } catch (Exception e) {
                Log.e("ALL_BOOKINGS", "Parse error: " + e.getMessage());
                Toast.makeText(AllBookingsActivity.this, "Error loading bookings", Toast.LENGTH_SHORT).show();
            }
        }

    }.execute();
}

}
