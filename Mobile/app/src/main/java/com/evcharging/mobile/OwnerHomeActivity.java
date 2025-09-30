package com.evcharging.mobile;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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

        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_owner_home);

                // Find the MapView by its ID
                mapView = findViewById(R.id.mapView);

                // Initialize the MapView
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

                btnReserve.setOnClickListener(v -> Toast
                                .makeText(OwnerHomeActivity.this, "Reserve Slot Clicked", Toast.LENGTH_SHORT).show());

                btnBookings.setOnClickListener(v -> Toast
                                .makeText(OwnerHomeActivity.this, "My Bookings Clicked", Toast.LENGTH_SHORT).show());

                btnHistory.setOnClickListener(v -> Toast
                                .makeText(OwnerHomeActivity.this, "Charging History Clicked", Toast.LENGTH_SHORT)
                                .show());
        }

        @Override
        public void onMapReady(@NonNull GoogleMap googleMap) {
                // Example marker at Colombo, Sri Lanka
                LatLng colombo = new LatLng(6.9271, 79.8612);
                googleMap.addMarker(new MarkerOptions().position(colombo).title("Charging Station"));
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(colombo, 12));
        }

        // MapView lifecycle
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
}
