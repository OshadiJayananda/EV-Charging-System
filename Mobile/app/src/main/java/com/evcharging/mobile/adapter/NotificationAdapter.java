package com.evcharging.mobile.adapter;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.evcharging.mobile.R;
import com.evcharging.mobile.model.Notification;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private List<Notification> notifications;
    private OnNotificationActionListener listener;

    public interface OnNotificationActionListener {
        void onMarkAsRead(String notificationId);

        void onDelete(String notificationId);
    }

    public NotificationAdapter(List<Notification> notifications, OnNotificationActionListener listener) {
        this.notifications = notifications;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notifications.get(position);
        holder.bind(notification);
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public void updateNotifications(List<Notification> newNotifications) {
        this.notifications.clear();
        this.notifications.addAll(newNotifications);
        notifyDataSetChanged();
    }

    public void removeNotification(int position) {
        notifications.remove(position);
        notifyItemRemoved(position);
    }

    public class NotificationViewHolder extends RecyclerView.ViewHolder {
        private TextView tvMessage;
        private TextView tvDate;
        private ImageButton btnMarkRead;
        private ImageButton btnDelete;
        private View readIndicator;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvNotificationMessage);
            tvDate = itemView.findViewById(R.id.tvNotificationDate);
            btnMarkRead = itemView.findViewById(R.id.btnMarkAsRead);
            btnDelete = itemView.findViewById(R.id.btnDeleteNotification);
            readIndicator = itemView.findViewById(R.id.viewReadIndicator);
        }

        public void bind(Notification notification) {
            tvMessage.setText(notification.getMessage());

            // Format date
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
            tvDate.setText(dateFormat.format(notification.getCreatedAt()));

            // Style based on read status
            if (notification.isRead()) {
                tvMessage.setTypeface(null, Typeface.NORMAL);
                tvMessage.setAlpha(0.7f);
                readIndicator.setVisibility(View.GONE);
                btnMarkRead.setVisibility(View.GONE);
            } else {
                tvMessage.setTypeface(null, Typeface.BOLD);
                tvMessage.setAlpha(1.0f);
                readIndicator.setVisibility(View.VISIBLE);
                btnMarkRead.setVisibility(View.VISIBLE);
            }

            // Set click listeners
            btnMarkRead.setOnClickListener(v -> {
                if (listener != null && !notification.isRead()) {
                    listener.onMarkAsRead(notification.getId());
                    notification.setRead(true);
                    notifyItemChanged(getAdapterPosition());
                }
            });

            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDelete(notification.getId());
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        removeNotification(position);
                    }
                }
            });

            // Make entire item clickable to mark as read
            itemView.setOnClickListener(v -> {
                if (listener != null && !notification.isRead()) {
                    listener.onMarkAsRead(notification.getId());
                    notification.setRead(true);
                    notifyItemChanged(getAdapterPosition());
                }
            });
        }
    }
}