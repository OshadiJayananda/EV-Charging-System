package com.evcharging.mobile;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Button;
import android.widget.ImageView;
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

        bookingId = getIntent().getStringExtra("bookingId");
        stationId = getIntent().getStringExtra("stationId");
        slotNumber = getIntent().getIntExtra("slotNumber", 0);
        startMs = getIntent().getLongExtra("start", 0);
        endMs = getIntent().getLongExtra("end", 0);
        status = getIntent().getStringExtra("status");
        qrBase64 = getIntent().getStringExtra("qrBase64");

        tvStatus.setText("Status: " + status);
        tvSlot.setText("Slot: #" + slotNumber);
        tvTime.setText("Time: " + fmt.format(new Date(startMs)) + " – " + fmt.format(new Date(endMs)));
        tvBookingId.setText("Booking ID: " + (bookingId != null ? bookingId : "-"));

        // Load QR if available
        if (qrBase64 != null && !qrBase64.isEmpty()) renderQr(qrBase64);

        // Fetch station name
        fetchStationName();

        // Setup swipe refresh
        swipeRefresh.setOnRefreshListener(this::refreshFromServer);

        // Share QR
        btnShareQr.setOnClickListener(v -> shareQr());
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshFromServer();
    }

    private void fetchStationName() {
        new AsyncTask<Void, Void, String>() {
            @Override protected String doInBackground(Void... voids) {
                ApiResponse res = api.getStationById(stationId);
                if (res == null || !res.isSuccess()) return null;
                try {
                    JSONObject o = new JSONObject(res.getData());
                    return o.optString("name", null);
                } catch (Exception e) { return null; }
            }
            @Override protected void onPostExecute(String name) {
                tvStation.setText("Station: " + (name != null ? name : "-"));
            }
        }.execute();
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
