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
        // Show loading or call API to get notifications
        // For now, showing empty state
        updateEmptyState();

        // TODO: Implement API call
        /*
         * apiClient.getUserNotifications(new ApiCallback<List<Notification>>() {
         * 
         * @Override
         * public void onSuccess(List<Notification> result) {
         * notifications.clear();
         * notifications.addAll(result);
         * adapter.notifyDataSetChanged();
         * updateEmptyState();
         * }
         * 
         * @Override
         * public void onError(String error) {
         * Toast.makeText(NotificationActivity.this, error, Toast.LENGTH_SHORT).show();
         * }
         * });
         */
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
        // TODO: Call API to mark notification as read
        Toast.makeText(this, "Marked as read", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDelete(String notificationId) {
        // TODO: Call API to delete notification
        Toast.makeText(this, "Notification deleted", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}