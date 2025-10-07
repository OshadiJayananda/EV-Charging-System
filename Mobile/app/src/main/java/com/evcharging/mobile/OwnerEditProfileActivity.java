package com.evcharging.mobile;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.evcharging.mobile.network.ApiClient;
import com.evcharging.mobile.network.ApiResponse;
import com.evcharging.mobile.session.SessionManager;
import org.json.JSONObject;

public class OwnerEditProfileActivity extends AppCompatActivity {
    private EditText etName, etEmail;
    private Button btnSaveChanges;
    private ApiClient apiClient;
    private SessionManager sessionManager;
    private String nic = "991234567V";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_edit_profile);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        btnSaveChanges = findViewById(R.id.btnSaveChanges);

        sessionManager = new SessionManager(this);
        apiClient = new ApiClient(sessionManager);

        btnSaveChanges.setOnClickListener(v -> new UpdateProfileTask().execute());
    }

    private class UpdateProfileTask extends AsyncTask<Void, Void, ApiResponse> {
        @Override
        protected ApiResponse doInBackground(Void... voids) {
            try {
                JSONObject data = new JSONObject();
                data.put("name", etName.getText().toString());
                data.put("email", etEmail.getText().toString());
                return apiClient.updateEvOwner(nic, data);
            } catch (Exception e) {
                return new ApiResponse(false, "JSON error", null);
            }
        }

        @Override
        protected void onPostExecute(ApiResponse response) {
            Toast.makeText(OwnerEditProfileActivity.this, response.getMessage(), Toast.LENGTH_SHORT).show();
            if (response.isSuccess())
                finish();
        }
    }
}