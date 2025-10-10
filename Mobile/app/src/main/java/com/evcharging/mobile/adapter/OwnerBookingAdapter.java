package com.evcharging.mobile.adapter;

import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.evcharging.mobile.R;
import com.evcharging.mobile.model.BookingItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OwnerBookingAdapter extends RecyclerView.Adapter<OwnerBookingAdapter.VH> {

    public interface OnBookingClick {
        void onClick(BookingItem item);
    }

    private final List<BookingItem> items = new ArrayList<>();
    private final OnBookingClick onClick;
    private final SimpleDateFormat fmt = new SimpleDateFormat("dd MMM, h:mm a", Locale.getDefault());

    public OwnerBookingAdapter(OnBookingClick onClick) {
        this.onClick = onClick;
    }

    public void setData(List<BookingItem> data) {
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.owner_booking_item, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        BookingItem b = items.get(pos);
        h.tvStation.setText(b.stationName != null ? b.stationName : "Station");
        h.tvSlot.setText(" • Slot #" + b.slotNumber);

        String when = fmt.format(new Date(b.startTimeMs)) + " – " + fmt.format(new Date(b.endTimeMs));
        h.tvWhen.setText(when);

        h.tvStatusPill.setText(b.status);
        int color = pillColor(b.status);
        h.tvStatusPill.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_IN);

        h.itemView.setOnClickListener(v -> onClick.onClick(b));
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvStation, tvWhen, tvStatusPill, tvSlot;
        VH(@NonNull View v) {
            super(v);
            tvStation = v.findViewById(R.id.tvStation);
            tvWhen = v.findViewById(R.id.tvWhen);
            tvStatusPill = v.findViewById(R.id.tvStatusPill);
            tvSlot = v.findViewById(R.id.tvSlot);
        }
    }

    private int pillColor(String status) {
        if (status == null) return 0xFF9E9E9E;
        switch (status) {
            case "Pending":   return 0xFFFFC107; // amber
            case "Approved":  return 0xFF4CAF50; // green
            case "Charging":  return 0xFF2196F3; // blue
            case "Finalized": return 0xFF4CAF50; // green
            case "Canceled":  return 0xFFF44336; // red
            case "Expired":   return 0xFF9E9E9E; // grey
            default:          return 0xFF9E9E9E;
        }
    }
}
