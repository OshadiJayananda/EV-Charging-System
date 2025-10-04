package com.evcharging.mobile;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.evcharging.mobile.adapter.NotificationAdapter;
import com.evcharging.mobile.model.Notification;
import com.evcharging.mobile.network.ApiClient;
import com.evcharging.mobile.network.ApiResponse;
import com.evcharging.mobile.session.SessionManager;
import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends AppCompatActivity
        implements NotificationAdapter.OnNotificationActionListener {

    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private List<Notification> notifications;
    private LinearLayout layoutEmptyState;
    private ApiClient apiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Notifications");

        recyclerView = findViewById(R.id.recyclerViewNotifications);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);

        notifications = new ArrayList<>();
        adapter = new NotificationAdapter(notifications, this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        apiClient = new ApiClient(new SessionManager(this));

        loadNotifications();
    }

    private void loadNotifications() {
        // Call API to get notifications in background thread
        new Thread(() -> {
            ApiResponse response = apiClient.getUserNotifications();

            // Switch back to UI thread to update views
            runOnUiThread(() -> {
                if (response.isSuccess() && response.getData() != null) {
                    // Parse the notifications from JSON response
                    List<Notification> notificationList = apiClient.parseNotifications(response.getData());

                    if (notificationList != null) {
                        notifications.clear();
                        notifications.addAll(notificationList);
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "Failed to parse notifications", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Show error message
                    String errorMessage = response.getMessage() != null ? response.getMessage() : "Failed to load notifications";
                    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
                }

                // Update empty state regardless of success/failure
                updateEmptyState();
            });
        }).start();
    }

    private void updateEmptyState() {
        if (notifications.isEmpty()) {
            layoutEmptyState.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            layoutEmptyState.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onMarkAsRead(String notificationId) {
        // Call API to mark notification as read in background thread
        new Thread(() -> {
            ApiResponse response = apiClient.markNotificationAsRead(notificationId);

            runOnUiThread(() -> {
                if (response.isSuccess()) {
                    Toast.makeText(this, "Marked as read", Toast.LENGTH_SHORT).show();
                    // Refresh notifications to get updated status
                    loadNotifications();
                } else {
                    String errorMessage = response.getMessage() != null ? response.getMessage() : "Failed to mark as read";
                    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    @Override
    public void onDelete(String notificationId) {
        // Call API to delete notification in background thread
        new Thread(() -> {
            ApiResponse response = apiClient.deleteNotification(notificationId);

            runOnUiThread(() -> {
                if (response.isSuccess()) {
                    Toast.makeText(this, "Notification deleted", Toast.LENGTH_SHORT).show();
                    // No need to refresh - adapter already removes item
                } else {
                    String errorMessage = response.getMessage() != null ? response.getMessage() : "Failed to delete notification";
                    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
                    // Refresh to restore deleted item on failure
                    loadNotifications();
                }
            });
        }).start();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
