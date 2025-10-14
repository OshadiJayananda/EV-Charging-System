package com.evcharging.mobile;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.evcharging.mobile.model.Notification;
import com.evcharging.mobile.service.SignalRService;

public class MyApp extends Application {

    private static final String CHANNEL_ID = "ev_notifications";

    private SignalRService signalRService;
    private final MutableLiveData<Notification> notificationLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> notificationCountLiveData = new MutableLiveData<>(0);
    private int notificationCount = 0;

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize SignalR service
        signalRService = new SignalRService(this);

        // Set listener to handle incoming notifications
        signalRService.setNotificationListener(this::onNotificationReceived);

        // Create notification channel
        createNotificationChannel();

        // Connect/disconnect SignalR based on app foreground/background
        ProcessLifecycleOwner.get().getLifecycle().addObserver((LifecycleEventObserver) (source, event) -> {
            if (event == Lifecycle.Event.ON_START) {
                if (!signalRService.isConnected()) {
                    signalRService.connect();
                }
            } else if (event == Lifecycle.Event.ON_STOP) {
                signalRService.disconnect();
            }
        });
    }

    public LiveData<Notification> getNotificationLiveData() {
        return notificationLiveData;
    }

    public LiveData<Integer> getNotificationCountLiveData() {
        return notificationCountLiveData;
    }

    public void onNotificationReceived(Notification notification) {
        if (notification == null)
            return;

        // Publish notification to observers
        notificationLiveData.postValue(notification);

        // Update and publish count
        notificationCount++;
        notificationCountLiveData.postValue(notificationCount);

        // Show a Toast on UI thread
        new Handler(Looper.getMainLooper()).post(
                () -> Toast.makeText(getApplicationContext(), notification.getMessage(), Toast.LENGTH_LONG).show());

        // Show system notification
        showSystemNotification(notification);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "EV Charging Notifications";
            String description = "Notifications for EV charging system";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void showSystemNotification(Notification notification) {
        Intent intent = new Intent(this, NotificationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle("EV Charging System")
                .setContentText(notification.getMessage())
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManager mgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (mgr != null) {
            mgr.notify((int) System.currentTimeMillis(), builder.build());
        }
    }

    public void resetNotificationCount() {
        notificationCount = 0;
        notificationCountLiveData.postValue(notificationCount);
    }

    public void markNotificationAsRead(String notificationId) {
        if (notificationCount > 0) {
            notificationCount--;
            notificationCountLiveData.postValue(notificationCount);
        }
    }
}
