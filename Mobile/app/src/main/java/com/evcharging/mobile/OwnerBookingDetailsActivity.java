package com.evcharging.mobile;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
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
import java.util.Locale;

public class OwnerBookingDetailsActivity extends AppCompatActivity {

    private SwipeRefreshLayout swipeRefresh;
    private TextView tvStatus, tvStation, tvSlot, tvTime, tvBookingId, tvQrNote;
    private ImageView ivQr, ivStatusIcon;
    private Button btnShareQr;
    private LinearLayout qrContainer, statusContainer;
    private CardView cardDetails;

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_booking_details);

        session = new SessionManager(this);
        api = new ApiClient(session);
        apiClient = new ApiClient(session);

        initializeViews();
        setupAnimations();

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

        // Set bookingId for later use
        if (currentBooking != null) {
            bookingId = currentBooking.getBookingId();
        }

        // --- Display Data with enhanced UI ---
        displayBookingData();

        // Swipe refresh
        swipeRefresh.setOnRefreshListener(this::refreshFromServer);

        // Share QR
        btnShareQr.setOnClickListener(v -> shareQr());

        setupFooterNavigation();
        highlightActiveTab("bookings");
    }

    private void initializeViews() {
        swipeRefresh = findViewById(R.id.swipeRefresh);
        tvStatus = findViewById(R.id.tvStatus);
        tvStation = findViewById(R.id.tvStation);
        tvSlot = findViewById(R.id.tvSlot);
        tvTime = findViewById(R.id.tvTime);
        tvBookingId = findViewById(R.id.tvBookingId);
        ivQr = findViewById(R.id.ivQr);
        btnShareQr = findViewById(R.id.btnShareQr);
        tvReason = findViewById(R.id.tvReason);
        tvQrNote = findViewById(R.id.tvQrNote);
        ivStatusIcon = findViewById(R.id.ivStatusIcon);
        qrContainer = findViewById(R.id.qrContainer);
        statusContainer = findViewById(R.id.statusContainer);
        cardDetails = findViewById(R.id.cardDetails);
    }

    private void setupAnimations() {
        AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(600);
        cardDetails.startAnimation(fadeIn);
    }

    private void displayBookingData() {
        if (currentBooking != null) {
            String bookingStatus = currentBooking.getStatus() != null ? currentBooking.getStatus() : "Pending";

            // Set status with enhanced styling
            updateStatusUI(bookingStatus);

            // Handle cancellation reason
            if (bookingStatus.equalsIgnoreCase("Cancelled")) {
                String reason = currentBooking.getCancellationReason();
                if (reason != null && !reason.isEmpty()) {
                    tvReason.setVisibility(View.VISIBLE);
                    tvReason.setText("Reason: " + reason);
                } else {
                    tvReason.setVisibility(View.VISIBLE);
                    tvReason.setText("Reason: Slot is under Maintenance");
                }
            } else {
                tvReason.setVisibility(View.GONE);
            }

            // Set other booking details
            tvStation.setText(currentBooking.getStationName() != null ? currentBooking.getStationName() : "-");
            tvSlot.setText(currentBooking.getSlotNumber() != null ? "Slot " + currentBooking.getSlotNumber() : "-");
            tvBookingId.setText(currentBooking.getBookingId() != null ? currentBooking.getBookingId() : "-");

            try {
                String startTime = currentBooking.getStartTimeFormatted();
                String endTime = currentBooking.getEndTimeFormatted();
                if (startTime != null && endTime != null) {
                    tvTime.setText(startTime + " – " + endTime);
                } else {
                    tvTime.setText("Time information not available");
                }
            } catch (Exception e) {
                tvTime.setText("Time: -");
            }

            // Handle QR display based on status
            handleQrDisplay(bookingStatus);
        }
    }

    private void updateStatusUI(String status) {
        int statusColor;
        int statusIcon;
        String statusText = status;

        switch (status.toLowerCase()) {
            case "approved":
                statusColor = Color.parseColor("#4CAF50"); // Green
                statusIcon = android.R.drawable.presence_online; // Green check icon
                statusText = "✓ " + status;
                break;
            case "charging":
                statusColor = Color.parseColor("#2196F3"); // Blue for active charging
                statusIcon = android.R.drawable.stat_sys_download; // Download/charging icon
                statusText = "⚡ " + status;
                break;
            case "pending":
                statusColor = Color.parseColor("#FF9800"); // Orange
                statusIcon = android.R.drawable.ic_popup_sync; // Sync/loading icon
                statusText = "⏳ " + status;
                break;
            case "finalized":
                statusColor = Color.parseColor("#2196F3"); // Blue
                statusIcon = android.R.drawable.ic_menu_edit; // Edit/complete icon
                statusText = "✅ " + status;
                break;
            case "cancelled":
                statusColor = Color.parseColor("#F44336"); // Red
                statusIcon = android.R.drawable.ic_delete; // Delete/cancel icon
                statusText = "❌ " + status;
                break;
            case "expired":
                statusColor = Color.parseColor("#9E9E9E"); // Gray
                statusIcon = android.R.drawable.ic_lock_idle_alarm; // Alarm/clock icon
                statusText = "⏰ " + status;
                break;
            default:
                statusColor = Color.parseColor("#757575");
                statusIcon = android.R.drawable.ic_dialog_info; // Info icon
        }

        tvStatus.setText(statusText);
        tvStatus.setTextColor(statusColor);

        // Update status container background
        statusContainer.setBackgroundColor(statusColor);

        // Set status icon if available
        if (ivStatusIcon != null) {
            ivStatusIcon.setImageResource(statusIcon);
            ivStatusIcon.setColorFilter(Color.WHITE);
        }

        // Add pulse animation for pending and charging status
        if (status.equalsIgnoreCase("pending") || status.equalsIgnoreCase("charging")) {
            tvStatus.setAlpha(0.7f);
            tvStatus.animate().alpha(1.0f).setDuration(1000).start();
        }
    }

    private void handleQrDisplay(String status) {
        switch (status.toLowerCase()) {
            case "approved":
                // Show QR for approved bookings
                if (currentBooking.getQrImageBase64() != null && !currentBooking.getQrImageBase64().isEmpty()) {
                    renderQr(currentBooking.getQrImageBase64());
                    tvQrNote.setText("Show this QR code to the station operator for charging access");
                    tvQrNote.setTextColor(Color.parseColor("#4CAF50"));
                    btnShareQr.setVisibility(View.VISIBLE);
                    qrContainer.setVisibility(View.VISIBLE);
                } else {
                    // If approved but no QR, show loading state
                    tvQrNote.setText("QR code loading...");
                    tvQrNote.setTextColor(Color.parseColor("#FF9800"));
                    btnShareQr.setVisibility(View.GONE);
                    qrContainer.setVisibility(View.VISIBLE);
                    ivQr.setImageResource(android.R.drawable.ic_menu_gallery); // Placeholder icon
                }
                break;

            case "charging":
                // Show currently charging message
                tvQrNote.setText("You are Currently Charging");
                tvQrNote.setTextColor(Color.parseColor("#2196F3"));
                btnShareQr.setVisibility(View.GONE);
                qrContainer.setVisibility(View.GONE);
                break;

            case "pending":
                // Show note for pending bookings
                tvQrNote.setText("You will receive the QR code once your booking is approved by the station owner");
                tvQrNote.setTextColor(Color.parseColor("#FF9800"));
                btnShareQr.setVisibility(View.GONE);
                qrContainer.setVisibility(View.GONE);
                break;

            case "finalized":
            case "cancelled":
            case "expired":
                // Show inactive message for other statuses
                tvQrNote.setText("Your booking QR code is no longer active");
                tvQrNote.setTextColor(Color.parseColor("#F44336"));
                btnShareQr.setVisibility(View.GONE);
                qrContainer.setVisibility(View.GONE);
                break;

            default:
                tvQrNote.setText("QR code status unavailable");
                tvQrNote.setTextColor(Color.parseColor("#757575"));
                btnShareQr.setVisibility(View.GONE);
                qrContainer.setVisibility(View.GONE);
        }

        // Add fade animation for QR note
        AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(500);
        tvQrNote.startAnimation(fadeIn);
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
            @Override
            protected JSONObject doInBackground(Void... voids) {
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

            @Override
            protected void onPostExecute(JSONObject o) {
                swipeRefresh.setRefreshing(false);
                if (o == null) return;

                String newStatus = o.optString("status", status);
                updateStatusUI(newStatus);

                String b64 = o.optString("qrImageBase64", null);
                if (b64 != null && !b64.isEmpty()) {
                    renderQr(b64);
                }
                handleQrDisplay(newStatus);
            }
        }.execute();
    }

    private void refreshBookingDetails() {
        if (currentBooking == null || currentBooking.getBookingId() == null) {
            return;
        }

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
                    updateStatusUI(newStatus);
                    handleQrDisplay(newStatus);

                    // Update QR code if available
                    String newQrBase64 = obj.optString("qrImageBase64", null);
                    if (newQrBase64 != null && !newQrBase64.isEmpty()) {
                        renderQr(newQrBase64);
                    }
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

            // Add fade-in animation for QR
            AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
            fadeIn.setDuration(800);
            ivQr.startAnimation(fadeIn);
        } catch (Exception e) {
            e.printStackTrace();
            // Show placeholder if QR rendering fails
            ivQr.setImageResource(android.R.drawable.ic_menu_gallery); // Use system gallery icon as placeholder
        }
    }

    private void shareQr() {
        if (qrBitmap == null) {
            // Show message if no QR available
            android.widget.Toast.makeText(this, "QR code not available for sharing", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

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
            shareIntent.putExtra(Intent.EXTRA_TEXT, "EV Charging Booking QR Code\nBooking ID: " + bookingId + "\nStation: " + currentBooking.getStationName());
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(shareIntent, "Share QR Code via"));

        } catch (Exception e) {
            e.printStackTrace();
            android.widget.Toast.makeText(this, "Failed to share QR code", android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up bitmap to avoid memory leaks
        if (qrBitmap != null && !qrBitmap.isRecycled()) {
            qrBitmap.recycle();
            qrBitmap = null;
        }
    }
}