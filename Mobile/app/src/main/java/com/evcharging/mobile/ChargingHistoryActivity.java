package com.evcharging.mobile;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.evcharging.mobile.adapter.OwnerBookingAdapter;
import com.evcharging.mobile.model.BookingItem;
import com.evcharging.mobile.model.User;
import com.evcharging.mobile.network.ApiClient;
import com.evcharging.mobile.network.ApiResponse;
import com.evcharging.mobile.session.SessionManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ChargingHistoryActivity extends AppCompatActivity {

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerViewHistory;
    private TextView tvEmpty;

    private SessionManager session;
    private ApiClient apiClient;
    private OwnerBookingAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charging_history);

        // Initialize
        session = new SessionManager(this);
        apiClient = new ApiClient(session);

        swipeRefreshLayout = findViewById(R.id.swipe);
        recyclerViewHistory = findViewById(R.id.rvHistory);
        tvEmpty = findViewById(R.id.tvEmpty);

        // Setup Adapter + RecyclerView
        adapter = new OwnerBookingAdapter(new ArrayList<>(), item -> {
            Intent i = new Intent(this, OwnerBookingDetailsActivity.class);
            i.putExtra("bookingId", item.getBookingId());
            i.putExtra("stationId", item.getStationId());
            i.putExtra("stationName", item.getStationName());
            i.putExtra("slotNumber", item.getSlotNumber());
            i.putExtra("start", item.getStartTime());
            i.putExtra("end", item.getEndTime());
            i.putExtra("status", item.getStatus());
            i.putExtra("qrBase64", item.getQrImageBase64());
            startActivity(i);
        });

        recyclerViewHistory.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewHistory.setAdapter(adapter);

        swipeRefreshLayout.setOnRefreshListener(this::loadData);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    /**
     * Load past (Finalized/Expired/Canceled) bookings
     */
    private void loadData() {
        swipeRefreshLayout.setRefreshing(true);

        new AsyncTask<Void, Void, List<BookingItem>>() {
            @Override
            protected List<BookingItem> doInBackground(Void... voids) {
                try {
                    // Fetch owner ID
                    User loggedUser = session.getLoggedInUser();
                    String ownerId = (loggedUser != null) ? loggedUser.getUserId() : null;
                    if (ownerId == null || ownerId.isEmpty()) return null;

                    ApiResponse res = apiClient.getBookingsByOwner(ownerId);
                    if (res == null || !res.isSuccess()) return null;

                    JSONArray arr = new JSONArray(res.getData());
                    List<BookingItem> historyList = new ArrayList<>();

                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject o = arr.getJSONObject(i);
                        BookingItem b = new BookingItem();

                        b.setBookingId(o.optString("bookingId", o.optString("_id", null)));
                        b.setStationId(o.optString("stationId", o.optString("StationId", null)));
                        b.setStationName(o.optString("stationName", ""));
                        b.setSlotId(o.optString("slotId"));
                        b.setSlotNumber(o.optString("slotNumber", o.optString("slotNo", "")));
                        b.setTimeSlotId(o.optString("timeSlotId"));
                        b.setOwnerId(o.optString("ownerId"));
                        b.setStatus(o.optString("status"));
                        b.setStartTime(o.optString("startTime"));
                        b.setEndTime(o.optString("endTime"));
                        b.setQrImageBase64(o.optString("qrImageBase64", null));

                        // Include only past bookings
                        if ("Finalized".equalsIgnoreCase(b.getStatus()) ||
                                "Cancelled".equalsIgnoreCase(b.getStatus()) ||
                                "Expired".equalsIgnoreCase(b.getStatus())) {
                            historyList.add(b);
                        }
                    }
                    return historyList;

                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(List<BookingItem> data) {
                swipeRefreshLayout.setRefreshing(false);

                if (data == null || data.isEmpty()) {
                    tvEmpty.setVisibility(View.VISIBLE);
                    adapter.setData(new ArrayList<>());
                } else {
                    tvEmpty.setVisibility(View.GONE);
                    adapter.setData(data);
                }
            }
        }.execute();
    }
}
