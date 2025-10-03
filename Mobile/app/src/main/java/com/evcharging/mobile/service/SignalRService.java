package com.evcharging.mobile.service;

import android.content.Context;
import android.util.Log;
import com.evcharging.mobile.model.Notification;
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
        String serverUrl = "https://7601b8b9448a.ngrok-free.app/notificationHub"; // Replace with your server URL

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
        if (hubConnection.getConnectionState() == HubConnectionState.DISCONNECTED) {
            hubConnection.start().blockingAwait();
            Log.d(TAG, "SignalR connected");
        }
    }

    public void disconnect() {
        if (hubConnection.getConnectionState() == HubConnectionState.CONNECTED) {
            hubConnection.stop();
            Log.d(TAG, "SignalR disconnected");
        }
    }

    public void setNotificationListener(NotificationListener listener) {
        this.notificationListener = listener;
    }

    public boolean isConnected() {
        return hubConnection.getConnectionState() == HubConnectionState.CONNECTED;
    }
}