package com.evcharging.mobile.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class BookingRepository {
    private DatabaseHelper dbHelper;

    public BookingRepository(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    // Insert or Update booking
    public void saveBooking(String id, String stationId, String ownerId, String status, String startTime, String endTime) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_BK_ID, id);
        values.put(DatabaseHelper.COL_BK_STATION_ID, stationId);
        values.put(DatabaseHelper.COL_BK_OWNER_ID, ownerId);
        values.put(DatabaseHelper.COL_BK_STATUS, status);
        values.put(DatabaseHelper.COL_BK_START, startTime);
        values.put(DatabaseHelper.COL_BK_END, endTime);

        db.insertWithOnConflict(DatabaseHelper.TABLE_BOOKING, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    // Fetch all bookings
    public Cursor getAllBookings() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        return db.query(DatabaseHelper.TABLE_BOOKING, null, null, null, null, null, DatabaseHelper.COL_BK_START + " ASC");
    }

    // Fetch booking by ID
    public Cursor getBookingById(String id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        return db.query(DatabaseHelper.TABLE_BOOKING,
                null,
                DatabaseHelper.COL_BK_ID + "=?",
                new String[]{id},
                null, null, null);
    }

    // Delete booking
    public void deleteBooking(String id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_BOOKING, DatabaseHelper.COL_BK_ID + "=?", new String[]{id});
        db.close();
    }

    // Clear all
    public void clearBookings() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_BOOKING, null, null);
        db.close();
    }
}
