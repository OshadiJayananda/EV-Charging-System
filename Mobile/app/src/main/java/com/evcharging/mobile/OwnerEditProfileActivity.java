package com.evcharging.mobile;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.evcharging.mobile.network.ApiResponse;
import com.evcharging.mobile.service.OwnerService;
import com.evcharging.mobile.session.SessionManager;

public class OwnerEditProfileActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPhone;
    private Button btnSaveChanges;
    private OwnerService ownerService;

    // For testing - you can replace this with sessionManager.getNic() later
    private String nic = "2000123456";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_edit_profile);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        btnSaveChanges = findViewById(R.id.btnSaveChanges);

        ownerService = new OwnerService();

        btnSaveChanges.setOnClickListener(v -> new UpdateProfileTask().execute());
    }

    private class UpdateProfileTask extends AsyncTask<Void, Void, ApiResponse> {
        @Override
        protected ApiResponse doInBackground(Void... voids) {
            @SuppressLint("WrongThread")
            String fullName = etName.getText().toString().trim();
            @SuppressLint("WrongThread")
            String email = etEmail.getText().toString().trim();
            @SuppressLint("WrongThread")
            String phone = etPhone.getText().toString().trim();

            if (fullName.isEmpty() || email.isEmpty() || phone.isEmpty()) {
                return new ApiResponse(false, "All fields are required", null);
            }

            return ownerService.updateEvOwner(nic, fullName, email, phone);
        }

        @Override
        protected void onPostExecute(ApiResponse response) {
            Toast.makeText(OwnerEditProfileActivity.this, response.getMessage(), Toast.LENGTH_SHORT).show();
            if (response.isSuccess()) {
                finish();
            }
        }
    }
}
