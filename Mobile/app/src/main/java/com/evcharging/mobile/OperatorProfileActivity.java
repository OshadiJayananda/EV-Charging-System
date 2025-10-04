package com.evcharging.mobile;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class OperatorProfileActivity extends AppCompatActivity {

    private TextView tvName, tvEmail, tvOperatorId;
    private ImageView ivProfilePic;
    private Button btnEditProfile;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operator_profile);

        // Views
        tvName = findViewById(R.id.tvOperatorName);
        tvEmail = findViewById(R.id.tvOperatorEmail);
        tvOperatorId = findViewById(R.id.tvOperatorId);
        ivProfilePic = findViewById(R.id.ivProfilePic);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnBack = findViewById(R.id.btnBack);

        // Example data (later you can load from DB or API)
        tvName.setText("John Doe");
        tvEmail.setText("johndoe@gmail.com");
        tvOperatorId.setText("OW001");

        // Back button action
        btnBack.setOnClickListener(v -> finish());

        // Edit button action
        btnEditProfile.setOnClickListener(v ->
                Toast.makeText(this, "Edit Profile Clicked", Toast.LENGTH_SHORT).show()
        );
    }
}