package com.evcharging.mobile;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.evcharging.mobile.R;
import com.evcharging.mobile.model.User;
import com.evcharging.mobile.session.SessionManager;

public class OperatorProfileActivity extends AppCompatActivity {

    private TextView tvName, tvEmail, tvRole, tvUserId, tvStation, tvStationLoc, tvActive;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operator_profile);
        setTitle("Operator Profile");

        session = new SessionManager(this);
        bindViews();
        loadUserData();
    }

    private void bindViews() {
        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        tvRole = findViewById(R.id.tvRole);
        tvUserId = findViewById(R.id.tvUserId);
        tvStation = findViewById(R.id.tvStation);
        tvStationLoc = findViewById(R.id.tvStationLoc);
        tvActive = findViewById(R.id.tvActive);
    }

    private void loadUserData() {
        User u = session.getLoggedInUser();
        if (u == null) return;

        tvName.setText(u.getFullName());
        tvEmail.setText(u.getEmail());
        tvRole.setText(u.getRoleDisplayName());
        tvUserId.setText(u.getUserId());

        // Show "Pending" if station not assigned
        tvStation.setText(
                (u.getStationName() != null && !u.getStationName().equals("string"))
                        ? u.getStationName()
                        : "Pending"
        );

        tvStationLoc.setText(
                (u.getStationLocation() != null && !u.getStationLocation().equals("string"))
                        ? u.getStationLocation()
                        : "Pending"
        );

        tvActive.setText(u.isActive() ? "Active" : "Inactive");
    }
}
