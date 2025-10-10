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
import com.evcharging.mobile.model.Station;
import com.evcharging.mobile.network.ApiClient;
import com.evcharging.mobile.network.ApiResponse;
import com.evcharging.mobile.session.SessionManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class OwnerBookingsActivity extends AppCompatActivity {

    private SwipeRefreshLayout swipe;
    private RecyclerView rv;
    private TextView tvEmpty;

    private SessionManager session;
    private ApiClient api;
    private OwnerBookingAdapter adapter;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_owner_bookings);

        session = new SessionManager(this);
        api = new ApiClient(session);

        swipe = findViewById(R.id.swipe);
        rv = findViewById(R.id.rvBookings);
        tvEmpty = findViewById(R.id.tvEmpty);

        adapter = new OwnerBookingAdapter(item -> {
            Intent i = new Intent(this, OwnerBookingDetailsActivity.class);
            i.putExtra("bookingId", item.bookingId);
            i.putExtra("stationId", item.stationId);
            i.putExtra("slotNumber", item.slotNumber);
            i.putExtra("start", item.startTimeMs);
            i.putExtra("end", item.endTimeMs);
            i.putExtra("status", item.status);
            i.putExtra("qrBase64", item.qrImageBase64); // may be null in list; details will refetch if needed
            startActivity(i);
        });

        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        swipe.setOnRefreshListener(this::loadData);
    }

    @Override protected void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        swipe.setRefreshing(true);
        new AsyncTask<Void, Void, List<BookingItem>>() {
            @Override protected List<BookingItem> doInBackground(Void... voids) {
                String ownerId = session.getLoggedInUser() != null ? session.getLoggedInUser().getUserId() : null;
                ApiResponse res = api.getBookingsByOwner(ownerId);
                if (res == null || !res.isSuccess()) return null;

                try {
                    JSONArray arr = new JSONArray(res.getData());
                    List<BookingItem> list = new ArrayList<>();
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject o = arr.getJSONObject(i);
                        BookingItem b = new BookingItem();
                        b.bookingId     = o.optString("bookingId", o.optString("_id", null));
                        b.stationId     = o.optString("stationId");
                        b.stationName   = o.optString("stationName", null);
                        b.slotId        = o.optString("slotId");
                        b.slotNumber    = o.optInt("slotNumber", o.optInt("slotNo", 0));
                        b.timeSlotId    = o.optString("timeSlotId");
                        b.ownerId       = o.optString("ownerId");
                        b.status        = o.optString("status");
                        b.startTimeMs   = o.optLong("startTimeMs", o.optJSONObject("startTime") != null ? o.optJSONObject("startTime").optLong("$date", 0) : o.optLong("startTime", 0));
                        b.endTimeMs     = o.optLong("endTimeMs",   o.optJSONObject("endTime")   != null ? o.optJSONObject("endTime").optLong("$date", 0)   : o.optLong("endTime", 0));
                        b.qrCode        = o.optString("qrCode", null);
                        b.qrExpiresAtMs = o.optLong("qrExpiresAt", 0);
                        b.qrImageBase64 = o.optString("qrImageBase64", null);
                        // Filter: only upcoming
                        if ("Pending".equals(b.status) || "Approved".equals(b.status) || "Charging".equals(b.status)) {
                            list.add(b);
                        }
                    }
                    return list;
                } catch (Exception ignore) {
                    return null;
                }
            }

            @Override protected void onPostExecute(List<BookingItem> data) {
                swipe.setRefreshing(false);
                adapter.setData(data);
                tvEmpty.setVisibility(data == null || data.isEmpty() ? View.VISIBLE : View.GONE);
            }
        }.execute();
    }
}
