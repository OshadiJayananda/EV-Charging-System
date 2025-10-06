package com.evcharging.mobile.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BookingRepository {
    private final DatabaseHelper dbHelper;

    public BookingRepository(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    // Save bookings to the database
    public void saveBookings(JSONArray bookingsArray) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (int i = 0; i < bookingsArray.length(); i++) {
                JSONObject obj = bookingsArray.getJSONObject(i);

                ContentValues values = new ContentValues();
                values.put(DatabaseHelper.COL_BK_ID, obj.optString("bookingId", ""));
                values.put(DatabaseHelper.COL_BK_STATION_ID, obj.optString("stationId", ""));
                values.put(DatabaseHelper.COL_BK_OWNER_ID, obj.optString("ownerId", ""));
                values.put(DatabaseHelper.COL_BK_STATUS, obj.optString("status", ""));
                values.put(DatabaseHelper.COL_BK_START, obj.optString("startTime", ""));
                values.put(DatabaseHelper.COL_BK_END, obj.optString("endTime", ""));

                // Insert booking into the database
                long rowId = db.insertWithOnConflict(DatabaseHelper.TABLE_BOOKING, null, values, SQLiteDatabase.CONFLICT_REPLACE);

                Log.d("BOOKING_DB", "Inserted booking with ID: " + obj.optString("bookingId", "") + ", rowId: " + rowId);
            }
            db.setTransactionSuccessful();
            Log.d("BOOKING_DB", "Saved " + bookingsArray.length() + " bookings to SQLite");
        } catch (Exception e) {
            Log.e("BOOKING_DB", "Error saving bookings", e);
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    // Fetch all bookings from the database
    public Cursor getAllBookings() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        return db.query(DatabaseHelper.TABLE_BOOKING, null, null, null, null, null, null);
    }

    // Clear all saved bookings from the database
    public void clearBookings() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_BOOKING, null, null);
        db.close();
    }
}
