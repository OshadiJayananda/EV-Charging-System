package com.evcharging.mobile;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.evcharging.mobile.adapter.OwnerBookingAdapter;
import com.evcharging.mobile.model.BookingItem;
import com.evcharging.mobile.network.ApiClient;
import com.evcharging.mobile.network.ApiResponse;
import com.evcharging.mobile.session.SessionManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class OwnerBookingsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private OwnerBookingAdapter adapter;
    private List<BookingItem> bookings = new ArrayList<>();
    private ApiClient apiClient;
    private SessionManager session;
    private final Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_bookings);
        setTitle("My Bookings");

        recyclerView = findViewById(R.id.recyclerViewBookings);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        session = new SessionManager(this);
        apiClient = new ApiClient(session);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OwnerBookingAdapter(bookings, this::openDetails);
        recyclerView.setAdapter(adapter);

        swipeRefreshLayout.setOnRefreshListener(this::fetchBookings);
        fetchBookings();
    }

    private void fetchBookings() {
        swipeRefreshLayout.setRefreshing(true);

        new AsyncTask<Void, Void, ApiResponse>() {
            @Override
            protected ApiResponse doInBackground(Void... voids) {
                try {
                    String ownerId = session.getUserId(); // Make sure this matches OwnerId in DB
                    return apiClient.get("/bookings/owner/" + ownerId);
                } catch (Exception e) {
                    Log.e("OwnerBookings", "Error fetching bookings", e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(ApiResponse res) {
                swipeRefreshLayout.setRefreshing(false);
                if (res == null || !res.isSuccess()) {
                    Toast.makeText(OwnerBookingsActivity.this, "Failed to load bookings", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    Type listType = new TypeToken<List<BookingItem>>() {}.getType();
                    List<BookingItem> fetched = gson.fromJson(res.getData(), listType);

                    // Show only Pending / Approved / Charging
                    bookings.clear();
                    for (BookingItem b : fetched) {
                        if (b.getStatus().equalsIgnoreCase("Pending")
                                || b.getStatus().equalsIgnoreCase("Approved")
                                || b.getStatus().equalsIgnoreCase("Charging")) {
                            bookings.add(b);
                        }
                    }

                    adapter.notifyDataSetChanged();
                } catch (Exception e) {
                    Log.e("OwnerBookings", "Parse error", e);
                    Toast.makeText(OwnerBookingsActivity.this, "Error parsing bookings", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }

    private void openDetails(BookingItem booking) {
        Intent intent = new Intent(this, OwnerBookingDetailsActivity.class);
        intent.putExtra("booking", gson.toJson(booking));
        startActivity(intent);
    }
}
