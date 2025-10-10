package com.evcharging.mobile;

import android.app.DatePickerDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.evcharging.mobile.model.Station;
import com.evcharging.mobile.model.SlotItem;
import com.evcharging.mobile.model.TimeSlotItem;
import com.evcharging.mobile.network.ApiClient;
import com.evcharging.mobile.network.ApiResponse;
import com.evcharging.mobile.session.SessionManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;

public class OwnerBookingActivity extends AppCompatActivity {

    private Spinner spnType, spnStation, spnSlot, spnTimeSlot;
    private Button btnSelectDate, btnConfirmBooking;
    private TextView tvSelectedDate, tvHints;
    private Date selectedDate;

    private SessionManager sessionManager;
    private ApiClient apiClient;
    private final Gson gson = new Gson();

    private String selectedType = "AC";
    private String selectedStationId;
    private String selectedSlotId;
    private String selectedTimeSlotId;
    private String selectedDateStr; // yyyy-MM-dd

    private List<Station> stations = new ArrayList<>();
    private List<SlotItem> slots = new ArrayList<>();
    private List<TimeSlotItem> timeSlots = new ArrayList<>();

    // Demo location
    private static final double DEFAULT_LAT = 6.9908661;
    private static final double DEFAULT_LON = 79.9395566;
    private static final double DEFAULT_RADIUS = 10.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_booking);

        sessionManager = new SessionManager(this);
        apiClient = new ApiClient(sessionManager);

        bindViews();
        setupTypeSpinner();
        setupDatePicker();
        setupConfirm();
    }

    private void bindViews() {
        spnType = findViewById(R.id.spnType);
        spnStation = findViewById(R.id.spnStation);
        spnSlot = findViewById(R.id.spnSlot);
        spnTimeSlot = findViewById(R.id.spnTimeSlot);
        btnSelectDate = findViewById(R.id.btnSelectDate);
        btnConfirmBooking = findViewById(R.id.btnConfirmBooking);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        tvHints = findViewById(R.id.tvHints);
    }

    private void setupTypeSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, new String[]{"AC", "DC"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnType.setAdapter(adapter);
        spnType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                selectedType = (String) parent.getItemAtPosition(pos);
                loadStationsByType(selectedType);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadStationsByType(String selectedType) {
        Toast.makeText(this, "Fetching nearby " + selectedType + " stations...", Toast.LENGTH_SHORT).show();

        Executors.newSingleThreadExecutor().execute(() -> {
            ApiResponse res = null;
            try {
                res = apiClient.getNearbyStationsByType(selectedType, DEFAULT_LAT, DEFAULT_LON, DEFAULT_RADIUS);
            } catch (Exception e) {
                Log.e("OwnerBooking", "Error fetching stations", e);
            }

            ApiResponse finalRes = res;
            runOnUiThread(() -> {
                if (finalRes == null || !finalRes.isSuccess()) {
                    toast("Failed to fetch stations");
                    return;
                }

                try {
                    JSONArray arr = new JSONArray(finalRes.getData());

                    stations.clear();

                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject obj = arr.getJSONObject(i);
                        Station s = new Station();
                        s.setStationId(obj.optString("stationId"));
                        s.setName(obj.optString("name"));
                        s.setLocation(obj.optString("location"));
                        s.setLatitude(obj.optDouble("latitude"));
                        s.setLongitude(obj.optDouble("longitude"));
                        s.setType(obj.optString("type"));

                        stations.add(s);
                    }

                    if (stations.isEmpty()) {
                        toast("No nearby " + selectedType + " stations found");
                        return;
                    }

                    ArrayAdapter<String> stnAdapter = new ArrayAdapter<>(
                            this, android.R.layout.simple_spinner_item,
                            stations.stream().map(st -> st.getName()).toArray(String[]::new)
                    );
                    stnAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spnStation.setAdapter(stnAdapter);

                    spnStation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            selectedStationId = stations.get(position).getStationId();
                            clearSlots();
                            clearTimeSlots();
                        }

                        @Override public void onNothingSelected(AdapterView<?> parent) {}
                    });

                    toast(arr.length() + " stations found");

                } catch (Exception e) {
                    Log.e("OwnerBooking", "JSON parse error", e);
                    toast("Error parsing station data");
                }
            });
        });
    }

    private void setupDatePicker() {
        btnSelectDate.setOnClickListener(v -> {
            Calendar now = Calendar.getInstance();
            DatePickerDialog dialog = new DatePickerDialog(
                    this,
                    (view, year, month, day) -> {
                        Calendar sel = Calendar.getInstance();
                        sel.set(year, month, day, 0, 0, 0);
                        selectedDate = sel.getTime();
                        selectedDateStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDate);
                        tvSelectedDate.setText(selectedDateStr);

                        if (selectedStationId == null) {
                            toast("Please select a station");
                            return;
                        }
                        loadSlotsForStation(selectedStationId);
                    },
                    now.get(Calendar.YEAR),
                    now.get(Calendar.MONTH),
                    now.get(Calendar.DAY_OF_MONTH)
            );

            Calendar min = Calendar.getInstance();
            Calendar max = Calendar.getInstance();
            max.add(Calendar.DAY_OF_YEAR, 6);
            dialog.getDatePicker().setMinDate(min.getTimeInMillis());
            dialog.getDatePicker().setMaxDate(max.getTimeInMillis());
            dialog.show();
        });
    }

    private void loadSlotsForStation(String stationId) {
        clearSlots();

        new AsyncTask<Void, Void, ApiResponse>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                Toast.makeText(OwnerBookingActivity.this, "Fetching slots...", Toast.LENGTH_SHORT).show();
            }

            @Override
            protected ApiResponse doInBackground(Void... voids) {
                try {
                    // Try normal slot endpoint first
                    ApiResponse res = apiClient.getSlotsByStation(stationId);
                    if (res != null && res.isSuccess()) return res;

                    // Fallback: get public station details (if Owner can’t access slot endpoint)
                    return apiClient.getStationPublic(stationId);
                } catch (Exception e) {
                    Log.e("OwnerBooking", "Error fetching slots", e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(ApiResponse res) {
                super.onPostExecute(res);
                if (res == null) {
                    toast("Failed to fetch slots");
                    return;
                }

                List<SlotItem> slotList = new ArrayList<>();

                try {
                    JSONArray arr;
                    if (res.getData().trim().startsWith("[")) {
                        arr = new JSONArray(res.getData());
                    } else {
                        JSONObject stationObj = new JSONObject(res.getData());
                        arr = stationObj.optJSONArray("slots");
                    }

                    if (arr == null) {
                        tvHints.setText("No slots available for this station");
                        return;
                    }

                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject o = arr.getJSONObject(i);
                        SlotItem s = new SlotItem();
                        s.slotId = o.optString("slotId");
                        s.number = o.optString("number");
                        s.status = o.optString("status");
                        s.connectorType = o.optString("connectorType");
                        slotList.add(s);
                    }

                    if (slotList.isEmpty()) {
                        tvHints.setText("No slots found.");
                        return;
                    }

                    slots = slotList;

                    ArrayAdapter<String> slotAdapter = new ArrayAdapter<>(
                            OwnerBookingActivity.this,
                            android.R.layout.simple_spinner_item,
                            slots.stream().map(SlotItem::toString).toArray(String[]::new)
                    );
                    slotAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spnSlot.setAdapter(slotAdapter);

                    spnSlot.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            selectedSlotId = slots.get(position).slotId;
                            loadTimeslotsFor(stationId, selectedSlotId, selectedDateStr);
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {}
                    });

                } catch (Exception e) {
                    Log.e("OwnerBooking", "Parse slots failed", e);
                    tvHints.setText("Error parsing slot data.");
                }
            }
        }.execute();
    }


    private void loadTimeslotsFor(String stationId, String slotId, String dateYmd) {
        clearTimeSlots();

        new AsyncTask<Void, Void, ApiResponse>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                Toast.makeText(OwnerBookingActivity.this, "Fetching time slots...", Toast.LENGTH_SHORT).show();
            }

            @Override
            protected ApiResponse doInBackground(Void... voids) {
                try {
                    String endpoint = String.format("/timeslot?stationId=%s&slotId=%s&date=%s",
                            stationId, slotId, dateYmd);
                    return apiClient.get(endpoint);
                } catch (Exception e) {
                    Log.e("OwnerBooking", "Error fetching timeslots", e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(ApiResponse res) {
                super.onPostExecute(res);

                if (res == null) {
                    toast("Failed to fetch timeslots");
                    return;
                }

                if (!res.isSuccess()) {
                    toast("No timeslots available");
                    return;
                }

                try {
                    Type t = new TypeToken<List<TimeSlotItem>>(){}.getType();
                    List<TimeSlotItem> fetched = gson.fromJson(res.getData(), t);
                    if (fetched == null || fetched.isEmpty()) {
                        toast("No available time slots for this date");
                        return;
                    }

                    timeSlots = fetched;
                    ArrayAdapter<String> tsAdapter = new ArrayAdapter<>(
                            OwnerBookingActivity.this,
                            android.R.layout.simple_spinner_item,
                            timeSlots.stream().map(TimeSlotItem::toString).toArray(String[]::new)
                    );
                    tsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spnTimeSlot.setAdapter(tsAdapter);

                    spnTimeSlot.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            selectedTimeSlotId = timeSlots.get(position).timeSlotId;
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {}
                    });

                } catch (Exception e) {
                    Log.e("OwnerBooking", "Failed to parse timeslots", e);
                    toast("Timeslot parse error");
                }
            }
        }.execute();
    }


    private void setupConfirm() {
        btnConfirmBooking.setOnClickListener(v -> {
            if (selectedStationId == null) { toast("Select station"); return; }
            if (selectedDateStr == null) { toast("Select date"); return; }
            if (selectedSlotId == null) { toast("Select slot"); return; }
            if (selectedTimeSlotId == null) { toast("Select timeslot"); return; }

            new AsyncTask<Void, Void, ApiResponse>() {
                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    Toast.makeText(OwnerBookingActivity.this, "Creating booking...", Toast.LENGTH_SHORT).show();
                }

                @Override
                protected ApiResponse doInBackground(Void... voids) {
                    try {
                        return apiClient.createBooking(selectedStationId, selectedTimeSlotId, selectedSlotId);
                    } catch (Exception e) {
                        Log.e("OwnerBooking", "Booking error", e);
                        return null;
                    }
                }

                @Override
                protected void onPostExecute(ApiResponse res) {
                    super.onPostExecute(res);
                    if (res == null) {
                        toast("Network error while creating booking");
                        return;
                    }

                    if (!res.isSuccess()) {
                        toast("Booking failed: " + res.getMessage());
                        return;
                    }

                    toast("✅ Booking created successfully! QR generated.");
                    // Optional: navigate to confirmation / QR screen here
                    // Intent intent = new Intent(OwnerBookingActivity.this, BookingConfirmationActivity.class);
                    // intent.putExtra("bookingData", res.getData());
                    // startActivity(intent);
                }
            }.execute();
        });
    }


    private void clearStations() {
        stations.clear();
        spnStation.setAdapter(null);
        clearSlots();
        clearTimeSlots();
        selectedStationId = null;
    }

    private void clearSlots() {
        slots.clear();
        spnSlot.setAdapter(null);
        selectedSlotId = null;
    }

    private void clearTimeSlots() {
        timeSlots.clear();
        spnTimeSlot.setAdapter(null);
        selectedTimeSlotId = null;
    }

    private void toast(String m) { Toast.makeText(this, m, Toast.LENGTH_SHORT).show(); }
}
