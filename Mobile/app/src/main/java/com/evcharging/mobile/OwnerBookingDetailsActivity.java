package com.evcharging.mobile;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.evcharging.mobile.network.ApiClient;
import com.evcharging.mobile.network.ApiResponse;
import com.evcharging.mobile.session.SessionManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class OwnerBookingDetailsActivity extends AppCompatActivity {

    private SwipeRefreshLayout swipeRefresh;
    private TextView tvStatus, tvStation, tvSlot, tvTime, tvBookingId;
    private ImageView ivQr;
    private Button btnShareQr;

    private SessionManager session;
    private ApiClient api;

    private String bookingId, stationId, status, qrBase64;
    private int slotNumber;
    private long startMs, endMs;

    private final SimpleDateFormat fmt = new SimpleDateFormat("dd MMM yyyy, h:mm a", Locale.getDefault());
    private Bitmap qrBitmap;
    private com.evcharging.mobile.model.BookingItem currentBooking;
    private com.evcharging.mobile.network.ApiClient apiClient;
    private TextView tvReason;


    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_owner_booking_details);

        session = new SessionManager(this);
        api = new ApiClient(session);

        swipeRefresh = findViewById(R.id.swipeRefresh);
        tvStatus = findViewById(R.id.tvStatus);
        tvStation = findViewById(R.id.tvStation);
        tvSlot = findViewById(R.id.tvSlot);
        tvTime = findViewById(R.id.tvTime);
        tvBookingId = findViewById(R.id.tvBookingId);
        ivQr = findViewById(R.id.ivQr);
        btnShareQr = findViewById(R.id.btnShareQr);
        tvReason = findViewById(R.id.tvReason);


        // --- Handle both JSON and individual extras ---
        String bookingJson = getIntent().getStringExtra("booking");
        if (bookingJson != null) {
            // From OwnerBookingsActivity
            currentBooking = new com.google.gson.Gson().fromJson(bookingJson, com.evcharging.mobile.model.BookingItem.class);
        } else {
            // From ChargingHistoryActivity
            currentBooking = new com.evcharging.mobile.model.BookingItem();
            currentBooking.setBookingId(getIntent().getStringExtra("bookingId"));
            currentBooking.setStationName(getIntent().getStringExtra("stationName"));
            currentBooking.setSlotNumber(getIntent().getStringExtra("slotNumber"));
            currentBooking.setStatus(getIntent().getStringExtra("status"));
            currentBooking.setStartTime(getIntent().getStringExtra("start"));
            currentBooking.setEndTime(getIntent().getStringExtra("end"));
            currentBooking.setQrImageBase64(getIntent().getStringExtra("qrBase64"));
        }

        // --- Display Data ---
        if (currentBooking != null) {
            tvStatus.setText("Status: " + currentBooking.getStatus());
            if (currentBooking.getStatus().equalsIgnoreCase("Cancelled")) {
                String reason = currentBooking.getCancellationReason();
                if (reason != null && !reason.isEmpty()) {
                    tvReason.setVisibility(View.VISIBLE);
                    tvReason.setText("Reason: " + reason);
                } else {
                    tvReason.setVisibility(View.VISIBLE);
                    tvReason.setText("Reason: Slot is under Maintenance recorded");
                }
            } else {
                tvReason.setVisibility(View.GONE);
            }



            tvStation.setText("Station: " + (currentBooking.getStationName() != null ? currentBooking.getStationName() : "-"));
            tvSlot.setText("Slot:" + (currentBooking.getSlotNumber() != null ? currentBooking.getSlotNumber() : "-"));
            tvBookingId.setText("Booking ID: " + (currentBooking.getBookingId() != null ? currentBooking.getBookingId() : "-"));

            try {
                tvTime.setText(currentBooking.getStartTimeFormatted() + " – " + currentBooking.getEndTimeFormatted());
            } catch (Exception e) {
                tvTime.setText("Time: -");
            }

            if (currentBooking.getQrImageBase64() != null && !currentBooking.getQrImageBase64().isEmpty()) {
                renderQr(currentBooking.getQrImageBase64());
            }
        }


        // Swipe refresh
        swipeRefresh.setOnRefreshListener(this::refreshFromServer);

        // Share QR
        btnShareQr.setOnClickListener(v -> shareQr());

        setupFooterNavigation();
        highlightActiveTab("bookings");
    }
    // ---------------- Footer Navigation Setup ----------------
    private void setupFooterNavigation() {
        LinearLayout navHome = findViewById(R.id.navHome);
        LinearLayout navBookings = findViewById(R.id.navBookings);
        LinearLayout navProfile = findViewById(R.id.navProfile);

        if (navHome == null || navBookings == null || navProfile == null)
            return; // Footer not included on this layout

        navHome.setOnClickListener(v -> {
            Intent i = new Intent(this, OwnerHomeActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        });

        navBookings.setOnClickListener(v -> {
            Intent i = new Intent(this, OwnerBookingsActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        });

        navProfile.setOnClickListener(v -> {
            Intent i = new Intent(this, OwnerProfileActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        });
    }

    private void highlightActiveTab(String activeTab) {
        int activeColor = getResources().getColor(R.color.primary_dark);
        int inactiveColor = getResources().getColor(R.color.primary);

        LinearLayout navHome = findViewById(R.id.navHome);
        LinearLayout navBookings = findViewById(R.id.navBookings);
        LinearLayout navProfile = findViewById(R.id.navProfile);

        if (navHome == null || navBookings == null || navProfile == null)
            return;

        ImageView iconHome = navHome.findViewById(R.id.iconHome);
        ImageView iconBookings = navBookings.findViewById(R.id.iconBookings);
        ImageView iconProfile = navProfile.findViewById(R.id.iconProfile);

        TextView txtHome = navHome.findViewById(R.id.txtHome);
        TextView txtBookings = navBookings.findViewById(R.id.txtBookings);
        TextView txtProfile = navProfile.findViewById(R.id.txtProfile);

        iconHome.setColorFilter(inactiveColor);
        iconBookings.setColorFilter(inactiveColor);
        iconProfile.setColorFilter(inactiveColor);

        txtHome.setTextColor(inactiveColor);
        txtBookings.setTextColor(inactiveColor);
        txtProfile.setTextColor(inactiveColor);

        switch (activeTab) {
            case "home":
                iconHome.setColorFilter(activeColor);
                txtHome.setTextColor(activeColor);
                break;
            case "bookings":
                iconBookings.setColorFilter(activeColor);
                txtBookings.setTextColor(activeColor);
                break;
            case "profile":
                iconProfile.setColorFilter(activeColor);
                txtProfile.setTextColor(activeColor);
                break;
        }
    }

    // ----------------------------------------------------------


    @Override
    protected void onResume() {
        super.onResume();
        refreshBookingDetails();
    }


    private void refreshFromServer() {
        swipeRefresh.setRefreshing(true);
        new AsyncTask<Void, Void, JSONObject>() {
            @Override protected JSONObject doInBackground(Void... voids) {
                String ownerId = session.getLoggedInUser() != null ? session.getLoggedInUser().getUserId() : null;
                ApiResponse res = api.getBookingsByOwner(ownerId);
                if (res == null || !res.isSuccess()) return null;
                try {
                    JSONArray arr = new JSONArray(res.getData());
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject o = arr.getJSONObject(i);
                        String id = o.optString("bookingId", o.optString("_id", null));
                        if (bookingId != null && bookingId.equals(id)) return o;
                    }
                } catch (Exception ignored) {}
                return null;
            }

            @Override protected void onPostExecute(JSONObject o) {
                swipeRefresh.setRefreshing(false);
                if (o == null) return;
                status = o.optString("status", status);
                tvStatus.setText("Status: " + status);
                String b64 = o.optString("qrImageBase64", null);
                if (b64 != null && !b64.isEmpty()) renderQr(b64);
            }
        }.execute();
    }

    private void refreshBookingDetails() {
        new android.os.AsyncTask<Void, Void, com.evcharging.mobile.network.ApiResponse>() {
            @Override
            protected com.evcharging.mobile.network.ApiResponse doInBackground(Void... voids) {
                try {
                    String bookingId = currentBooking.getBookingId();
                    return apiClient.get("/bookings/" + bookingId);
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(com.evcharging.mobile.network.ApiResponse res) {
                if (res == null || !res.isSuccess()) return;

                try {
                    org.json.JSONObject obj = new org.json.JSONObject(res.getData());
                    String newStatus = obj.optString("status", "Pending");
                    tvStatus.setText("Status: " + newStatus);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }


    private void renderQr(String base64) {
        try {
            byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
            qrBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            ivQr.setImageBitmap(qrBitmap);
        } catch (Exception ignored) {}
    }

    private void shareQr() {
        if (qrBitmap == null) return;

        try {
            File cachePath = new File(getCacheDir(), "images");
            cachePath.mkdirs();
            File file = new File(cachePath, "qr_share.png");
            try (FileOutputStream stream = new FileOutputStream(file)) {
                qrBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            }

            Uri contentUri = FileProvider.getUriForFile(this,
                    getPackageName() + ".fileprovider", file);

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/png");
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Booking ID: " + bookingId);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(shareIntent, "Share QR Code via"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
