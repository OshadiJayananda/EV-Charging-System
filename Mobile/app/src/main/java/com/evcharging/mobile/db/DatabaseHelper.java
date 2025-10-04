package com.evcharging.mobile.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "evcharging.db";
    private static final int DATABASE_VERSION = 2; // ⚠️ increment version to force rebuild

    // Operator Table
    public static final String TABLE_OPERATOR = "Operator";
    public static final String COL_OP_ID = "id";
    public static final String COL_OP_NAME = "fullName";
    public static final String COL_OP_EMAIL = "email";
    public static final String COL_OP_STATION_ID = "stationId";
    public static final String COL_OP_STATION_NAME = "stationName";
    public static final String COL_OP_IS_ACTIVE = "isActive";

    // Booking Table
    public static final String TABLE_BOOKING = "Booking";
    public static final String COL_BK_ID = "id";
    public static final String COL_BK_STATION_ID = "stationId";
    public static final String COL_BK_OWNER_ID = "ownerId";
    public static final String COL_BK_STATUS = "status";
    public static final String COL_BK_START = "startTime";
    public static final String COL_BK_END = "endTime";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_OPERATOR_TABLE = "CREATE TABLE " + TABLE_OPERATOR + "("
                + COL_OP_ID + " TEXT PRIMARY KEY, "
                + COL_OP_NAME + " TEXT, "
                + COL_OP_EMAIL + " TEXT, "
                + COL_OP_STATION_ID + " TEXT, "
                + COL_OP_STATION_NAME + " TEXT, "
                + COL_OP_IS_ACTIVE + " INTEGER)";

        String CREATE_BOOKING_TABLE = "CREATE TABLE " + TABLE_BOOKING + "("
                + COL_BK_ID + " TEXT PRIMARY KEY, "
                + COL_BK_STATION_ID + " TEXT, "
                + COL_BK_OWNER_ID + " TEXT, "
                + COL_BK_STATUS + " TEXT, "
                + COL_BK_START + " TEXT, "
                + COL_BK_END + " TEXT)";

        db.execSQL(CREATE_OPERATOR_TABLE);
        db.execSQL(CREATE_BOOKING_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_OPERATOR);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKING);
        onCreate(db);
    }
}
