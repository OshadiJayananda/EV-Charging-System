package com.evcharging.mobile.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.evcharging.mobile.R;
import com.evcharging.mobile.model.BookingItem;

import java.util.ArrayList;
import java.util.List;

public class OwnerBookingAdapter extends RecyclerView.Adapter<OwnerBookingAdapter.ViewHolder> {

    public interface OnBookingClick {
        void onClick(BookingItem item);
    }

    private List<BookingItem> list;
    private final OnBookingClick listener;

    public OwnerBookingAdapter(List<BookingItem> list, OnBookingClick listener) {
        this.list = (list != null) ? list : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.owner_booking_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BookingItem item = list.get(position);

        holder.tvStationName.setText(item.getStationName() != null ? item.getStationName() : "Station");
        holder.tvSlotNumber.setText("Slot #" + (item.getSlotNumber() != null ? item.getSlotNumber() : "-"));
        holder.tvTime.setText(item.getStartTimeFormatted() + " - " + item.getEndTimeFormatted());
        holder.tvStatus.setText(item.getStatus());

        // Set status color dynamically
        switch (item.getStatus() != null ? item.getStatus() : "") {
            case "Approved":
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_green);
                break;
            case "Pending":
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_yellow);
                break;
            case "Charging":
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_blue);
                break;
            case "Finalized":
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_grey);
                break;
            default:
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_default);
                break;
        }

        holder.cardBooking.setOnClickListener(v -> listener.onClick(item));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    /**
     * Used to refresh adapter data dynamically
     */
    public void setData(List<BookingItem> newData) {
        this.list.clear();
        if (newData != null) {
            this.list.addAll(newData);
        }
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvStationName, tvSlotNumber, tvStatus, tvTime;
        CardView cardBooking;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStationName = itemView.findViewById(R.id.tvStationName);
            tvSlotNumber = itemView.findViewById(R.id.tvSlotNumber);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvTime = itemView.findViewById(R.id.tvTime);
            cardBooking = itemView.findViewById(R.id.cardBooking);
        }
    }
}
