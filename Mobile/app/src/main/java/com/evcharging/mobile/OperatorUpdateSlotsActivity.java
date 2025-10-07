package com.evcharging.mobile;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.evcharging.mobile.network.ApiClient;
import com.evcharging.mobile.network.ApiResponse;
import com.evcharging.mobile.session.SessionManager;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;

public class OperatorUpdateSlotsActivity extends AppCompatActivity {

    private SessionManager session;
    private ListView lvSlots;
    private SwipeRefreshLayout swipeRefresh;
    private ArrayList<HashMap<String, String>> slotList = new ArrayList<>();
    private SlotAdapter adapter;
    private ApiClient apiClient;
    private static final String TAG = "OperatorUpdateSlots";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_slots);
        setTitle("Update Slot Availability");

        session = new SessionManager(this);
        apiClient = new ApiClient(session); // ✅ correct constructor

        lvSlots = findViewById(R.id.lvSlots);
        swipeRefresh = findViewById(R.id.swipeRefresh);

        swipeRefresh.setOnRefreshListener(this::loadSlots);
        loadSlots();
    }

    // ✅ Load all slots for the operator’s station
    private void loadSlots() {
        swipeRefresh.setRefreshing(true);
        String stationId = session.getStationId();
        String url = "/slots/station/" + stationId;

        new Thread(() -> {
            try {
                ApiResponse response = apiClient.get(url); // ✅ proper ApiResponse
                Log.d(TAG, "GET /slots response: " + response.getMessage());

                if (response.isSuccess() && response.getData() != null) {
                    runOnUiThread(() -> {
                        parseSlots(response.getData().toString());
                        swipeRefresh.setRefreshing(false);
                    });
                } else {
                    runOnUiThread(() -> {
                        swipeRefresh.setRefreshing(false);
                        Toast.makeText(this, "Failed: " + response.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading slots", e);
                runOnUiThread(() -> {
                    swipeRefresh.setRefreshing(false);
                    Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void parseSlots(String responseData) {
        try {
            JSONArray arr = new JSONArray(responseData);
            slotList.clear();

            for (int i = 0; i < arr.length(); i++) {
                JSONObject s = arr.getJSONObject(i);
                HashMap<String, String> map = new HashMap<>();
                map.put("SlotId", s.optString("slotId"));
                map.put("Number", s.optString("number"));
                map.put("ConnectorType", s.optString("connectorType"));
                map.put("Status", s.optString("status"));
                slotList.add(map);
            }

            if (adapter == null) {
                adapter = new SlotAdapter();
                lvSlots.setAdapter(adapter);
            } else {
                adapter.notifyDataSetChanged();
            }

        } catch (Exception e) {
            Log.e(TAG, "Error parsing slot JSON", e);
        }
    }

    // ✅ Inner Adapter Class
    private class SlotAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return slotList.size();
        }

        @Override
        public Object getItem(int position) {
            return slotList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        // ✅ FIXED: Correct method signature
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = getLayoutInflater().inflate(R.layout.slot_list_item, parent, false);

            ImageView ivStatus = convertView.findViewById(R.id.ivStatus);
            TextView tvTitle = convertView.findViewById(R.id.tvSlotTitle);
            TextView tvSubtitle = convertView.findViewById(R.id.tvSlotSubtitle);

            HashMap<String, String> slot = slotList.get(position);
            String number = slot.get("Number");
            String type = slot.get("ConnectorType");
            String status = slot.get("Status");

            tvTitle.setText("Slot " + number + " (" + type + ")");
            tvSubtitle.setText("Status: " + status);

            if (status.equalsIgnoreCase("Available"))
                ivStatus.setColorFilter(0xFF00FF00);
            else if (status.equalsIgnoreCase("Booked"))
                ivStatus.setColorFilter(0xFFFF4444);
            else if (status.equalsIgnoreCase("Charging"))
                ivStatus.setColorFilter(0xFFFFBB33);
            else
                ivStatus.setColorFilter(0xFFAAAAAA);

            convertView.setOnClickListener(v -> confirmToggle(slot));

            return convertView;
        }
    }

    private void confirmToggle(HashMap<String, String> slot) {
        String status = slot.get("Status");
        if (status.equalsIgnoreCase("Charging")) {
            Toast.makeText(this, "Can't change an active slot!", Toast.LENGTH_SHORT).show();
            return;
        }

        String newStatus = status.equals("Available") ? "Booked" : "Available";
        new AlertDialog.Builder(this)
                .setTitle("Change Slot Status")
                .setMessage("Change Slot " + slot.get("Number") + " to " + newStatus + "?")
                .setPositiveButton("Yes", (d, w) -> toggleSlot(slot))
                .setNegativeButton("No", null)
                .show();
    }

    // ✅ PATCH with log response
    private void toggleSlot(HashMap<String, String> slot) {
        String slotId = slot.get("SlotId");
        String url = "/slots/" + slotId + "/toggle";

        new Thread(() -> {
            try {
                JSONObject emptyBody = new JSONObject();
                ApiResponse response = apiClient.patch(url, emptyBody);

                Log.d(TAG, "PATCH response message: " + response.getMessage());
                Log.d(TAG, "PATCH response data: " + response.getData());

                runOnUiThread(() -> {
                    if (response.isSuccess()) {
                        Toast.makeText(this, "Slot updated successfully", Toast.LENGTH_SHORT).show();
                        loadSlots(); // refresh view
                    } else {
                        Toast.makeText(this, "Failed: " + response.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error toggling slot", e);
                runOnUiThread(() ->
                        Toast.makeText(this, "Error updating slot", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}
