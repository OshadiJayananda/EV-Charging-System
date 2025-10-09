package com.evcharging.mobile;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

public class FooterHelper {
    public static void setupFooter(Activity activity) {
        View home = activity.findViewById(R.id.navHome);
        View bookings = activity.findViewById(R.id.navBookings);
        View profile = activity.findViewById(R.id.navProfile);

        if (home != null) {
            home.setOnClickListener(v -> {
                Intent i = new Intent(activity, OperatorHomeActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                activity.startActivity(i);
            });
        }

        if (bookings != null) {
            bookings.setOnClickListener(v -> {
                Intent i = new Intent(activity, AllBookingsActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                activity.startActivity(i);
            });
        }

        if (profile != null) {
            profile.setOnClickListener(v -> {
                Intent i = new Intent(activity, OperatorProfileActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                activity.startActivity(i);
            });
        }
    }
}
