package com.evcharging.mobile.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class OperatorRepository {
    private final DatabaseHelper dbHelper;

    public OperatorRepository(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public void saveOperator(String id, String fullName, String email, String stationId, String stationName, boolean isActive) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_OP_ID, id);
        values.put(DatabaseHelper.COL_OP_NAME, fullName);
        values.put(DatabaseHelper.COL_OP_EMAIL, email);
        values.put(DatabaseHelper.COL_OP_STATION_ID, stationId);
        values.put(DatabaseHelper.COL_OP_STATION_NAME, stationName);
        values.put(DatabaseHelper.COL_OP_IS_ACTIVE, isActive ? 1 : 0);

        db.insertWithOnConflict(DatabaseHelper.TABLE_OPERATOR, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    public Cursor getOperator() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        return db.query(DatabaseHelper.TABLE_OPERATOR, null, null, null, null, null, null);
    }

    public void clearOperator() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_OPERATOR, null, null);
        db.close();
    }
}
