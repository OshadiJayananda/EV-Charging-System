package com.evcharging.mobile;

import android.app.AlertDialog;
import android.graphics.Color;
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
        setTitle("Update Slot Status");

        session = new SessionManager(this);
        apiClient = new ApiClient(session);

        lvSlots = findViewById(R.id.lvSlots);
        swipeRefresh = findViewById(R.id.swipeRefresh);

        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        swipeRefresh.setOnRefreshListener(this::loadSlots);
        loadSlots();
    }

    private void loadSlots() {
        swipeRefresh.setRefreshing(true);
        String stationId = session.getStationId();
        String url = "/slots/station/" + stationId;

        new Thread(() -> {
            try {
                ApiResponse response = apiClient.get(url);
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

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = getLayoutInflater().inflate(R.layout.slot_list_item, parent, false);

            View vStatusIndicator = convertView.findViewById(R.id.vStatusIndicator);
            ImageView ivStatus = convertView.findViewById(R.id.ivStatus);
            TextView tvTitle = convertView.findViewById(R.id.tvSlotTitle);
            TextView tvSubtitle = convertView.findViewById(R.id.tvSlotSubtitle);
            TextView tvStatusText = convertView.findViewById(R.id.tvStatusText);

            HashMap<String, String> slot = slotList.get(position);
            String number = slot.get("Number");
            String type = slot.get("ConnectorType");
            String status = slot.get("Status");

            tvTitle.setText("Slot " + number);
            tvSubtitle.setText(type);

            // Modern status UI handling
            switch (status.toLowerCase()) {
                case "available":
                    vStatusIndicator.setBackgroundColor(Color.parseColor("#4CAF50"));
                    ivStatus.setImageResource(R.drawable.ic_check_circle);
                    ivStatus.setColorFilter(Color.parseColor("#4CAF50"));
                    tvStatusText.setTextColor(Color.parseColor("#4CAF50"));
                    tvStatusText.setText("Available");
                    break;

                case "booked":
                    vStatusIndicator.setBackgroundColor(Color.parseColor("#FFA726"));
                    ivStatus.setImageResource(R.drawable.ic_hourglass_empty);
                    ivStatus.setColorFilter(Color.parseColor("#FFA726"));
                    tvStatusText.setTextColor(Color.parseColor("#FFA726"));
                    tvStatusText.setText("Booked");
                    break;

                case "charging":
                    vStatusIndicator.setBackgroundColor(Color.parseColor("#1E88E5"));
                    ivStatus.setImageResource(R.drawable.ic_flash_on);
                    ivStatus.setColorFilter(Color.parseColor("#1E88E5"));
                    tvStatusText.setTextColor(Color.parseColor("#1E88E5"));
                    tvStatusText.setText("Charging");
                    break;

                case "under maintenance":
                    vStatusIndicator.setBackgroundColor(Color.parseColor("#FFC107"));
                    ivStatus.setImageResource(R.drawable.ic_build);
                    ivStatus.setColorFilter(Color.parseColor("#FFC107"));
                    tvStatusText.setTextColor(Color.parseColor("#FFC107"));
                    tvStatusText.setText("Maintenance");
                    break;

                case "out of order":
                    vStatusIndicator.setBackgroundColor(Color.parseColor("#E53935"));
                    ivStatus.setImageResource(R.drawable.ic_error);
                    ivStatus.setColorFilter(Color.parseColor("#E53935"));
                    tvStatusText.setTextColor(Color.parseColor("#E53935"));
                    tvStatusText.setText("Out of Order");
                    break;

                default:
                    vStatusIndicator.setBackgroundColor(Color.parseColor("#9E9E9E"));
                    ivStatus.setImageResource(R.drawable.ic_info);
                    ivStatus.setColorFilter(Color.parseColor("#9E9E9E"));
                    tvStatusText.setTextColor(Color.parseColor("#9E9E9E"));
                    tvStatusText.setText("Unknown");
                    break;
            }

            convertView.setOnClickListener(v -> showStatusChangeDialog(slot));
            return convertView;
        }
    }

    private void showStatusChangeDialog(HashMap<String, String> slot) {
        String currentStatus = slot.get("Status");

        if (currentStatus.equalsIgnoreCase("Charging")) {
            Toast.makeText(this, "Cannot change status of an active charging slot!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentStatus.equalsIgnoreCase("Booked")) {
            Toast.makeText(this, "Cannot change status of a booked slot!", Toast.LENGTH_SHORT).show();
            return;
        }

        final String[] statusOptions = {"Available", "Under Maintenance", "Out Of Order"};

        int currentIndex = 0;
        for (int i = 0; i < statusOptions.length; i++) {
            if (statusOptions[i].equalsIgnoreCase(currentStatus)) {
                currentIndex = i;
                break;
            }
        }

        new AlertDialog.Builder(this)
                .setTitle("Change Slot Status")
                .setSingleChoiceItems(statusOptions, currentIndex, null)
                .setPositiveButton("Update", (dialog, which) -> {
                    ListView lv = ((AlertDialog) dialog).getListView();
                    int selectedPosition = lv.getCheckedItemPosition();
                    if (selectedPosition >= 0) {
                        String newStatus = statusOptions[selectedPosition];
                        if (!newStatus.equalsIgnoreCase(currentStatus)) {
                            updateSlotStatus(slot, newStatus);
                        } else {
                            Toast.makeText(this, "Status unchanged", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateSlotStatus(HashMap<String, String> slot, String newStatus) {
        String slotId = slot.get("SlotId");
        String url = "/slots/" + slotId + "/status";

        new Thread(() -> {
            try {
                JSONObject body = new JSONObject();
                body.put("status", newStatus);

                ApiResponse response = apiClient.patch(url, body);

                Log.d(TAG, "PATCH response message: " + response.getMessage());
                Log.d(TAG, "PATCH response data: " + response.getData());

                runOnUiThread(() -> {
                    if (response.isSuccess()) {
                        Toast.makeText(this, "Slot status updated to: " + newStatus, Toast.LENGTH_SHORT).show();
                        loadSlots();
                    } else {
                        Toast.makeText(this, "Failed: " + response.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error updating slot status", e);
                runOnUiThread(() ->
                        Toast.makeText(this, "Error updating slot status", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}
