package com.evcharging.mobile.service;

import android.content.Context;
import android.util.Log;
import com.evcharging.mobile.model.Notification;
import com.evcharging.mobile.network.ApiClient;
import com.evcharging.mobile.session.SessionManager;
import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionBuilder;
import com.microsoft.signalr.HubConnectionState;

public class SignalRService {
    private static final String TAG = "SignalRService";
    private HubConnection hubConnection;
    private SessionManager sessionManager;
    private NotificationListener notificationListener;

    public interface NotificationListener {
        void onNotificationReceived(Notification notification);
    }

    public SignalRService(Context context) {
        sessionManager = new SessionManager(context);
        setupConnection();
    }

    private void setupConnection() {
        // Use the same base URL from ApiClient
        String serverUrl = ApiClient.getBaseUrl() + "/notificationHub";

        hubConnection = HubConnectionBuilder.create(serverUrl)
                .withHeader("Authorization", "Bearer " + sessionManager.getToken())
                .build();

        // Listen for notifications
        hubConnection.on("ReceiveNotification", (notification) -> {
            Log.d(TAG, "Notification received: " + notification.getMessage());
            if (notificationListener != null) {
                notificationListener.onNotificationReceived(notification);
            }
        }, Notification.class);
    }

    public void connect() {
        try {
            if (hubConnection.getConnectionState() == HubConnectionState.DISCONNECTED) {
                hubConnection.start().blockingAwait();
                Log.d(TAG, "SignalR connected");
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to connect to SignalR", e);
        }
    }

    public void disconnect() {
        try {
            if (hubConnection.getConnectionState() == HubConnectionState.CONNECTED) {
                hubConnection.stop();
                Log.d(TAG, "SignalR disconnected");
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to disconnect from SignalR", e);
        }
    }

    public void setNotificationListener(NotificationListener listener) {
        this.notificationListener = listener;
    }

    public boolean isConnected() {
        return hubConnection != null && hubConnection.getConnectionState() == HubConnectionState.CONNECTED;
    }

    // Method to reconnect with new token (useful after login)
    public void reconnectWithNewToken() {
        disconnect();
        setupConnection();
        connect();
    }
}