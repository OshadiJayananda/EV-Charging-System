package com.evcharging.mobile;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.evcharging.mobile.R;

public class BookingConfirmationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_confirmation);

        ImageView ivQr = findViewById(R.id.ivQr);
        TextView tvMessage = findViewById(R.id.tvMessage);

        tvMessage.setText(R.string.booking_confirmed_message);

        String qrBase64 = getIntent().getStringExtra("qrBitmap");
        if (qrBase64 != null && !qrBase64.isEmpty()) {
            byte[] decodedBytes = Base64.decode(qrBase64, Base64.DEFAULT);
            Bitmap qrBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            ivQr.setImageBitmap(qrBitmap);
        }
    }
}
