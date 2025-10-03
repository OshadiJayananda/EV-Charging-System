package com.evcharging.mobile;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class OwnerProfileActivity extends AppCompatActivity {

    private TextView tvName, tvEmail, tvOwnerId;
    private ImageView ivProfilePic;
    private Button btnEditProfile;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_profile);

        // Views
        tvName = findViewById(R.id.tvOwnerName);
        tvEmail = findViewById(R.id.tvOwnerEmail);
        tvOwnerId = findViewById(R.id.tvOwnerId);
        ivProfilePic = findViewById(R.id.ivProfilePic);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnBack = findViewById(R.id.btnBack);

        // Example data (later you can load from DB or API)
        tvName.setText("John Doe");
        tvEmail.setText("johndoe@gmail.com");
        tvOwnerId.setText("OW001");

        // Back button action
        btnBack.setOnClickListener(v -> finish());

        // Edit button action
        btnEditProfile.setOnClickListener(v ->
                Toast.makeText(this, "Edit Profile Clicked", Toast.LENGTH_SHORT).show()
        );
    }
}
