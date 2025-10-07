package com.evcharging.mobile;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
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
                ArrayList<String> list = new ArrayList<>();

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);

                    String id = obj.optString("bookingId", "N/A");
                    String status = obj.optString("status", "N/A");
                    String start = obj.optString("formattedStartTime", obj.optString("startTime", "N/A"));
                    String end = obj.optString("formattedEndTime", obj.optString("endTime", "N/A"));

                    list.add("ID: " + id + "\nStart: " + start + "\nEnd: " + end + "\nStatus: " + status);
                }

                if (list.isEmpty()) {
                    list.add("No upcoming bookings found for this station");
                }

                lvAllBookings.setAdapter(
                        new ArrayAdapter<>(AllBookingsActivity.this, android.R.layout.simple_list_item_1, list)
                );

            } catch (Exception e) {
                Log.e("ALL_BOOKINGS", "Parse error: " + e.getMessage());
                Toast.makeText(AllBookingsActivity.this, "Error loading bookings", Toast.LENGTH_SHORT).show();
            }
        }
    }.execute();
}

}
