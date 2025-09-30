package com.evcharging.mobile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class OperatorHomeActivity extends AppCompatActivity {

        Button btnUpdateSlots, btnViewBookings, btnCancelBookings;
        ListView lvTodayReservations;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_operator_home);

                lvTodayReservations = findViewById(R.id.lvTodayReservations);
                btnUpdateSlots = findViewById(R.id.btnUpdateSlots);
                btnViewBookings = findViewById(R.id.btnViewBookings);
                btnCancelBookings = findViewById(R.id.btnCancelBookings);

                // Hardcoded sample reservations for today
                String[] reservations = {
                                "10:00 AM - EV1234 - Station A",
                                "11:30 AM - EV5678 - Station B",
                                "01:00 PM - EV9012 - Station C",
                                "03:30 PM - EV3456 - Station A"
                };

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                this,
                                android.R.layout.simple_list_item_1,
                                reservations);

                lvTodayReservations.setAdapter(adapter);

                // Button actions
                btnUpdateSlots.setOnClickListener(v -> Toast
                                .makeText(OperatorHomeActivity.this, "Update Slots Clicked", Toast.LENGTH_SHORT).show()
                // startActivity(new Intent(OperatorHomeActivity.this,
                // UpdateSlotActivity.class))
                );

                btnViewBookings.setOnClickListener(v -> Toast
                                .makeText(OperatorHomeActivity.this, "View Bookings Clicked", Toast.LENGTH_SHORT).show()
                // startActivity(new Intent(OperatorHomeActivity.this, BookingsActivity.class))
                );

                btnCancelBookings.setOnClickListener(v -> Toast
                                .makeText(OperatorHomeActivity.this, "Cancel Bookings Clicked", Toast.LENGTH_SHORT)
                                .show()
                // startActivity(new Intent(OperatorHomeActivity.this, BookingsActivity.class))
                );
        }
}
