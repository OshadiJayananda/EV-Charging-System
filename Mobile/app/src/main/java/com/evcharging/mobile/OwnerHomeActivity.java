package com.evcharging.mobile;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.evcharging.mobile.model.Notification;
import com.evcharging.mobile.model.User;
import com.evcharging.mobile.network.ApiClient;
import com.evcharging.mobile.network.ApiResponse;
import com.evcharging.mobile.service.SignalRService;
import com.evcharging.mobile.session.SessionManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import android.location.Location;
import com.evcharging.mobile.model.Station;
import com.evcharging.mobile.service.StationService;
import java.util.List;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class OwnerHomeActivity extends AppCompatActivity
                implements OnMapReadyCallback, SignalRService.NotificationListener {

        private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";
        private static final String CHANNEL_ID = "ev_notifications";
        private FusedLocationProviderClient fusedLocationClient;

        private MapView mapView;
        private Button btnReserve, btnBookings, btnHistory;
        private ImageButton btnLogout, btnNotifications;
        private ImageView ivProfile;
        private TextView tvWelcomeOwner, tvOwnerId;

        private SignalRService signalRService;
        private ApiClient apiClient;
        private TextView tvNotificationCount;
        private int notificationCount = 0;

        private GoogleMap googleMap;
        private StationService stationService;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);

                apiClient = new ApiClient(new SessionManager(this));
                stationService = new StationService(apiClient);

                setContentView(R.layout.activity_owner_home);

                // Initialize UI components
                mapView = findViewById(R.id.mapView);
                btnReserve = findViewById(R.id.btnReserveSlot);
                btnBookings = findViewById(R.id.btnMyBookings);
                btnHistory = findViewById(R.id.btnChargingHistory);
                ivProfile = findViewById(R.id.ivProfileOwner);
                btnLogout = findViewById(R.id.btnLogoutOwner);
                btnNotifications = findViewById(R.id.btnNotifications);
                tvNotificationCount = findViewById(R.id.tvNotificationCount);

                tvWelcomeOwner = findViewById(R.id.tvWelcomeOwner);
                tvOwnerId = findViewById(R.id.tvOwnerId);

                String ownerName = getOwnerName();
                String ownerId = getOwnerId();

                tvWelcomeOwner.setText("Welcome, " + ownerName + "!");
                tvOwnerId.setText("Owner ID: " + ownerId);

                fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

                // Initialize MapView
                Bundle mapViewBundle = null;
                if (savedInstanceState != null) {
                        mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
                }
                mapView.onCreate(mapViewBundle);
                mapView.getMapAsync(this);

                // Initialize services
                signalRService = new SignalRService(this);
                signalRService.setNotificationListener(this);

                // Create notification channel
                createNotificationChannel();

                // Button actions
                setupButtonActions();
        }

        private String getOwnerName() {
                SessionManager sessionManager = new SessionManager(this);

                User loggedInUser = sessionManager.getLoggedInUser();

                String ownerName = "EV Owner";

                if (loggedInUser != null && loggedInUser.getFullName() != null) {
                        String fullName = loggedInUser.getFullName().trim();
                        // Split by space and take the first part
                        String[] nameParts = fullName.split("\\s+");
                        ownerName = nameParts.length > 0 ? nameParts[0] : fullName;
                }
                return ownerName;
        }

        private String getOwnerId() {
                SessionManager sessionManager = new SessionManager(this);
                User loggedInUser = sessionManager.getLoggedInUser();

                String ownerId = "OW001";

                if (loggedInUser != null && loggedInUser.getUserId() != null
                                && !loggedInUser.getUserId().trim().isEmpty()) {
                        ownerId = "OW" + loggedInUser.getUserId().trim();
                }

                return ownerId;
        }

        private void setupButtonActions() {
                btnNotifications.setOnClickListener(v -> startActivity(new Intent(this, NotificationActivity.class)));

                btnReserve.setOnClickListener(
                                v -> Toast.makeText(this, "Reserve Slot Clicked", Toast.LENGTH_SHORT).show());

                btnBookings.setOnClickListener(
                                v -> Toast.makeText(this, "My Bookings Clicked", Toast.LENGTH_SHORT).show());

                btnHistory.setOnClickListener(
                                v -> Toast.makeText(this, "Charging History Clicked", Toast.LENGTH_SHORT).show());

                ivProfile.setOnClickListener(v -> startActivity(new Intent(this, OwnerProfileActivity.class)));

                btnLogout.setOnClickListener(v -> attemptLogout());
        }

        @Override
        public void onMapReady(@NonNull GoogleMap map) {
                Log.d("OwnerHomeActivity", "onMapReady() called");

                this.googleMap = map;

                // Check permissions first
                if (ActivityCompat.checkSelfPermission(this,
                                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                        ActivityCompat.requestPermissions(this,
                                        new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
                                        1001);
                        return;
                }

                googleMap.setMyLocationEnabled(true);
                googleMap.getUiSettings().setZoomControlsEnabled(true);
                googleMap.getUiSettings().setCompassEnabled(true);
                googleMap.getUiSettings().setMyLocationButtonEnabled(true);

                // Get current location (not cached)
                fusedLocationClient.getCurrentLocation(
                                com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
                                null).addOnSuccessListener(this, location -> {
                                        Log.d("OwnerHomeActivity", "Received location: " + location);

                                        if (location != null) {
                                                double userLat = location.getLatitude();
                                                double userLng = location.getLongitude();
                                                // Log.d("OwnerHomeActivity", "User location: Lat=" + userLat + " Lng="
                                                // + userLng);

                                                googleMap.moveCamera(CameraUpdateFactory
                                                                .newLatLngZoom(new LatLng(userLat, userLng), 15));

                                                LatLng userLocation = new LatLng(userLat, userLng);
                                                googleMap.addMarker(new MarkerOptions()
                                                                .position(userLocation)
                                                                .title("You are here"));

                                                // Fetch nearby stations in background thread
                                                new Thread(() -> {
                                                        List<Station> stations = stationService
                                                                        .getNearbyStations(userLat, userLng, 5);

                                                        runOnUiThread(() -> {
                                                                if (stations != null && !stations.isEmpty()) {
                                                                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                                                                        builder.include(userLocation); // include user's
                                                                                                       // location in
                                                                                                       // map bounds

                                                                        for (Station s : stations) {
                                                                                LatLng stationLatLng = new LatLng(
                                                                                                s.getLatitude(),
                                                                                                s.getLongitude());
                                                                                googleMap.addMarker(new MarkerOptions()
                                                                                                .position(stationLatLng)
                                                                                                .title(s.getName())
                                                                                                .snippet(s.getLocation()));
                                                                                builder.include(stationLatLng);
                                                                        }

                                                                        // Auto-adjust camera to include all markers
                                                                        LatLngBounds bounds = builder.build();
                                                                        int padding = 120; // space around edges
                                                                        googleMap.animateCamera(CameraUpdateFactory
                                                                                        .newLatLngBounds(bounds,
                                                                                                        padding));

                                                                } else {
                                                                        Toast.makeText(this, "No nearby stations found",
                                                                                        Toast.LENGTH_SHORT).show();
                                                                }
                                                        });
                                                }).start();

                                        } else {
                                                Log.e("OwnerHomeActivity", "Location is null");
                                                Toast.makeText(this,
                                                                "Unable to get location. Try setting GPS in emulator.",
                                                                Toast.LENGTH_SHORT).show();
                                        }
                                }).addOnFailureListener(e -> {
                                        Log.e("OwnerHomeActivity", "Failed to get location", e);
                                        Toast.makeText(this, "Error getting location", Toast.LENGTH_SHORT).show();
                                });
        }

        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                        @NonNull int[] grantResults) {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                if (requestCode == 1001) {
                        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                                // Try again now that permission is granted
                                onMapReady(googleMap);
                        } else {
                                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
                        }
                }
        }

        // Lifecycle methods for MapView and SignalR
        @Override
        protected void onStart() {
                super.onStart();
                mapView.onStart();
        }

        @Override
        protected void onResume() {
                super.onResume();
                mapView.onResume();

                // Connect to SignalR when activity resumes
                if (signalRService != null) {
                        signalRService.connect();
                }
        }

        @Override
        protected void onPause() {
                // Pause MapView before calling super
                mapView.onPause();
                super.onPause();

                // Disconnect from SignalR when activity pauses
                if (signalRService != null) {
                        signalRService.disconnect();
                }
        }

        @Override
        protected void onStop() {
                mapView.onStop();
                super.onStop();
        }

        @Override
        protected void onDestroy() {
                mapView.onDestroy();
                super.onDestroy();
        }

        @Override
        public void onLowMemory() {
                super.onLowMemory();
                mapView.onLowMemory();
        }

        @Override
        protected void onSaveInstanceState(@NonNull Bundle outState) {
                super.onSaveInstanceState(outState);
                Bundle mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY);
                if (mapViewBundle == null) {
                        mapViewBundle = new Bundle();
                        outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle);
                }
                mapView.onSaveInstanceState(mapViewBundle);
        }

        private void attemptLogout() {
                new androidx.appcompat.app.AlertDialog.Builder(this)
                                .setTitle("Logout Confirmation")
                                .setMessage("Are you sure you want to logout?")
                                .setPositiveButton("Yes", (dialog, which) -> {
                                        // Perform logout
                                        ApiResponse response = apiClient.logout();
                                        Toast.makeText(this, response.getMessage(), Toast.LENGTH_SHORT).show();

                                        // Redirect to login screen
                                        Intent intent = new Intent(this, LoginActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                                        | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        finish();
                                })
                                .setNegativeButton("No", (dialog, which) -> dialog.dismiss()) // Dismiss dialog if user
                                                                                              // cancels
                                .show();
        }

        @Override
        public void onNotificationReceived(Notification notification) {
                runOnUiThread(() -> {
                        // Show toast
                        notificationCount++;
                        updateNotificationCount();
                        Toast.makeText(this, notification.getMessage(), Toast.LENGTH_LONG).show();

                        // Show system notification
                        showSystemNotification(notification);
                });
        }

        private void createNotificationChannel() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        CharSequence name = "EV Charging Notifications";
                        String description = "Notifications for EV charging system";
                        int importance = NotificationManager.IMPORTANCE_DEFAULT;

                        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
                        channel.setDescription(description);

                        NotificationManager notificationManager = getSystemService(NotificationManager.class);
                        if (notificationManager != null) {
                                notificationManager.createNotificationChannel(channel);
                        }
                }
        }

        private void showSystemNotification(Notification notification) {
                NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                                .setSmallIcon(R.drawable.ic_notifications)
                                .setContentTitle("EV Charging System")
                                .setContentText(notification.getMessage())
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                .setAutoCancel(true);

                NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                if (notificationManager != null) {
                        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
                }
        }

        private void updateNotificationCount() {
                if (notificationCount > 0) {
                        tvNotificationCount.setVisibility(View.VISIBLE);
                        tvNotificationCount.setText(String.valueOf(notificationCount));
                } else {
                        tvNotificationCount.setVisibility(View.GONE);
                }
        }

}
