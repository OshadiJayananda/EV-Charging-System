package com.evcharging.mobile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.evcharging.mobile.network.ApiClient;
import com.evcharging.mobile.network.ApiResponse;
import com.evcharging.mobile.session.SessionManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class OwnerHomeActivity extends AppCompatActivity implements OnMapReadyCallback {

        private MapView mapView;
        private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";

        private Button btnReserve, btnBookings, btnHistory;
        private ImageButton btnLogout;
        private ImageView ivProfile;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_owner_home);

                // MapView setup
                mapView = findViewById(R.id.mapView);
                Bundle mapViewBundle = null;
                if (savedInstanceState != null) {
                        mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
                }
                mapView.onCreate(mapViewBundle);
                mapView.getMapAsync(this);

                // Buttons
                btnReserve = findViewById(R.id.btnReserveSlot);
                btnBookings = findViewById(R.id.btnMyBookings);
                btnHistory = findViewById(R.id.btnChargingHistory);

                // Profile & Logout
                ivProfile = findViewById(R.id.ivProfileOwner);
                btnLogout = findViewById(R.id.btnLogoutOwner);

                // Button Clicks
                btnReserve.setOnClickListener(
                                v -> Toast.makeText(this, "Reserve Slot Clicked", Toast.LENGTH_SHORT).show());

                btnBookings.setOnClickListener(
                                v -> Toast.makeText(this, "My Bookings Clicked", Toast.LENGTH_SHORT).show());

                btnHistory.setOnClickListener(
                                v -> Toast.makeText(this, "Charging History Clicked", Toast.LENGTH_SHORT).show());

                // Profile Click
                ivProfile.setOnClickListener(v -> {
                        startActivity(new Intent(this, OwnerProfileActivity.class));
                });

                // Logout Click
                btnLogout.setOnClickListener(v -> attemptLogout());
        }

        @Override
        public void onMapReady(@NonNull GoogleMap googleMap) {
                // Example marker at Colombo, Sri Lanka
                LatLng colombo = new LatLng(6.9271, 79.8612);
                googleMap.addMarker(new MarkerOptions().position(colombo).title("Charging Station"));
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(colombo, 12));
        }

        // MapView lifecycle methods
        @Override
        protected void onStart() {
                super.onStart();
                mapView.onStart();
        }

        @Override
        protected void onResume() {
                super.onResume();
                mapView.onResume();
        }

        @Override
        protected void onPause() {
                mapView.onPause();
                super.onPause();
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
                ApiClient apiClient = new ApiClient(new SessionManager(this));
                ApiResponse response = apiClient.logout();

                Toast.makeText(
                        this,
                        response.getMessage(),
                        Toast.LENGTH_SHORT).show();

                // Redirect to login screen after logout
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear backstack
                startActivity(intent);
                finish();
        }
}
