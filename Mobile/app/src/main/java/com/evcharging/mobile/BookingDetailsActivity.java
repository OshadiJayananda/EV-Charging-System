package com.evcharging.mobile;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.evcharging.mobile.network.ApiClient;
import com.evcharging.mobile.network.ApiResponse;
import com.evcharging.mobile.session.SessionManager;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class BookingDetailsActivity extends AppCompatActivity {

    private TextView tvBookingId, tvStatus, tvStartTime, tvEndTime;
    private ImageView ivQrCode;
    private Button btnScanQr, btnFinalize;
    private SessionManager session;
    private ApiClient apiClient;
    private String bookingId;
    private String currentStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_details);
        setTitle("Booking Details");

        session = new SessionManager(this);
        apiClient = new ApiClient(session);

        bindViews();

        // Retrieve data from intent
        bookingId = getIntent().getStringExtra("bookingId");
        currentStatus = getIntent().getStringExtra("status");
        String startTime = getIntent().getStringExtra("formattedStartTime");
        if (startTime == null || startTime.isEmpty())
            startTime = getIntent().getStringExtra("startTime");

        String endTime = getIntent().getStringExtra("formattedEndTime");
        if (endTime == null || endTime.isEmpty())
            endTime = getIntent().getStringExtra("endTime");

        String qrImageBase64 = getIntent().getStringExtra("qrImageBase64");

        tvBookingId.setText("Booking ID: " + bookingId);
        tvStatus.setText("Status: " + currentStatus);
        tvStartTime.setText("Start: " + startTime);
        tvEndTime.setText("End: " + endTime);

        // ✅ Show QR image if available
        if (qrImageBase64 != null && !qrImageBase64.isEmpty()) {
            byte[] decoded = Base64.decode(qrImageBase64, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
            ivQrCode.setImageBitmap(bitmap);
        } else {
            ivQrCode.setImageResource(android.R.drawable.ic_menu_report_image);
        }

        // Request camera permission if not granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 101);
        }

        btnScanQr.setOnClickListener(v -> startQrScanner());
        btnFinalize.setOnClickListener(v -> new FinalizeBookingTask().execute());
    }

    private void bindViews() {
        tvBookingId = findViewById(R.id.tvBookingId);
        tvStatus = findViewById(R.id.tvStatus);
        tvStartTime = findViewById(R.id.tvStartTime);
        tvEndTime = findViewById(R.id.tvEndTime);
        ivQrCode = findViewById(R.id.ivQrCode);
        btnScanQr = findViewById(R.id.btnScanQr);
        btnFinalize = findViewById(R.id.btnFinalize);
    }

    private void startQrScanner() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Scan Booking QR Code");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(com.journeyapps.barcodescanner.CaptureActivity.class);
        qrLauncher.launch(options);
    }

    private final ActivityResultLauncher<ScanOptions> qrLauncher = registerForActivityResult(new ScanContract(),
            result -> {
                if (result.getContents() == null) {
                    Toast.makeText(this, "Scan cancelled", Toast.LENGTH_SHORT).show();
                } else {
                    String scannedCode = result.getContents();
                    Log.d("QR_SCAN", "Scanned QR Code: " + scannedCode);

                    String qrCode = getIntent().getStringExtra("qrCode");
                    if (qrCode != null && scannedCode.trim().equalsIgnoreCase(qrCode.trim())) {
                        Toast.makeText(this, "QR matched! Starting charging...", Toast.LENGTH_SHORT).show();
                        new UpdateBookingStatusTask("Charging").execute();
                    } else {
                        Toast.makeText(this, "Invalid QR: does not match this booking", Toast.LENGTH_LONG).show();
                        Log.d("QR_SCAN", "Expected: " + qrCode + ", Got: " + scannedCode);
                    }
                }
            });

    private class UpdateBookingStatusTask extends AsyncTask<Void, Void, ApiResponse> {
        private final String newStatus;
        UpdateBookingStatusTask(String status) {
            this.newStatus = status;
        }

        @Override
        protected ApiResponse doInBackground(Void... voids) {
            try {
                return apiClient.patch("/bookings/" + bookingId + "/start", null);
            } catch (Exception e) {
                Log.e("BookingDetails", "Error starting booking", e);
                return new ApiResponse(false, "Error starting booking", null);
            }
        }

        @Override
        protected void onPostExecute(ApiResponse response) {
            if (response.isSuccess()) {
                Toast.makeText(BookingDetailsActivity.this, "Booking marked as Charging", Toast.LENGTH_SHORT).show();
                tvStatus.setText("Status: Charging");
            } else {
                Toast.makeText(BookingDetailsActivity.this, "Failed: " + response.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class FinalizeBookingTask extends AsyncTask<Void, Void, ApiResponse> {
        @Override
        protected ApiResponse doInBackground(Void... voids) {
            return apiClient.patch("/bookings/" + bookingId + "/finalize", null);
        }

        @Override
        protected void onPostExecute(ApiResponse response) {
            if (response.isSuccess()) {
                Toast.makeText(BookingDetailsActivity.this, "Booking finalized successfully", Toast.LENGTH_SHORT).show();
                tvStatus.setText("Status: Completed");
            } else {
                Toast.makeText(BookingDetailsActivity.this, "Finalize failed: " + response.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
