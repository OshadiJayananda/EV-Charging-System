package com.evcharging.mobile.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.evcharging.mobile.model.User;

/**
 * DatabaseHelper - SQLite database manager for local user data storage
 *
 * Purpose: Store logged-in user details locally to avoid repeated API calls
 * and maintain user session information across app restarts.
 *
 * Author: System
 * Created: 2025-10-06
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";

    // Database configuration
    private static final String DATABASE_NAME = "EVChargingApp.db";
    private static final int DATABASE_VERSION = 1;

    // Table name
    private static final String TABLE_USER = "user";

    // Column names
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_USER_ID = "user_id"; // NIC from JWT
    private static final String COLUMN_FULL_NAME = "full_name";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_ROLE = "role";
    private static final String COLUMN_STATION_ID = "station_id"; // Nullable for unassigned operators
    private static final String COLUMN_STATION_NAME = "station_name"; // Nullable
    private static final String COLUMN_STATION_LOCATION = "station_location"; // Nullable
    private static final String COLUMN_IS_ACTIVE = "is_active";
    private static final String COLUMN_CREATED_AT = "created_at";

    // Create table SQL statement
    private static final String CREATE_TABLE_USER = "CREATE TABLE " + TABLE_USER + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_USER_ID + " TEXT NOT NULL UNIQUE, " +
            COLUMN_FULL_NAME + " TEXT NOT NULL, " +
            COLUMN_EMAIL + " TEXT NOT NULL, " +
            COLUMN_ROLE + " TEXT NOT NULL, " +
            COLUMN_STATION_ID + " TEXT, " + // Nullable field
            COLUMN_STATION_NAME + " TEXT, " + // Nullable field
            COLUMN_STATION_LOCATION + " TEXT, " + // Nullable field
            COLUMN_IS_ACTIVE + " INTEGER DEFAULT 1, " +
            COLUMN_CREATED_AT + " TEXT NOT NULL" +
            ")";

    // Singleton instance
    private static DatabaseHelper instance;

    /**
     * Private constructor to enforce singleton pattern
     */
    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.d(TAG, "DatabaseHelper initialized");
    }

    /**
     * Get singleton instance of DatabaseHelper
     *
     * @param context Application context
     * @return DatabaseHelper instance
     */
    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Creating database tables");
        db.execSQL(CREATE_TABLE_USER);
        Log.d(TAG, "User table created successfully");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
        // Drop older table if exists and create fresh table
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        onCreate(db);
    }

    /**
     * Save or update user data in local database
     *
     * @param user User object containing user details
     * @return true if successful, false otherwise
     */
    public boolean saveUser(User user) {
        if (user == null) {
            Log.e(TAG, "Cannot save null user");
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_USER_ID, user.getUserId());
        values.put(COLUMN_FULL_NAME, user.getFullName());
        values.put(COLUMN_EMAIL, user.getEmail());
        values.put(COLUMN_ROLE, user.getRole());
        values.put(COLUMN_STATION_ID, user.getStationId()); // Can be null
        values.put(COLUMN_STATION_NAME, user.getStationName()); // Can be null
        values.put(COLUMN_STATION_LOCATION, user.getStationLocation()); // Can be null
        values.put(COLUMN_IS_ACTIVE, user.isActive() ? 1 : 0);
        values.put(COLUMN_CREATED_AT, user.getCreatedAt());

        try {
            // First, try to delete existing user data (replace if exists)
            db.delete(TABLE_USER, COLUMN_USER_ID + " = ?", new String[] { user.getUserId() });

            // Insert new user data
            long result = db.insert(TABLE_USER, null, values);

            if (result != -1) {
                Log.d(TAG, "User saved successfully: " + user.getEmail() + " (Role: " + user.getRole() + ")");
                if (user.getStationId() != null) {
                    Log.d(TAG, "Station assigned: " + user.getStationName() + " (ID: " + user.getStationId() + ")");
                } else {
                    Log.d(TAG, "No station assigned yet for operator");
                }
                return true;
            } else {
                Log.e(TAG, "Failed to save user");
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error saving user: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Retrieve logged-in user data from local database
     *
     * @return User object if found, null otherwise
     */
    public User getLoggedInUser() {
        SQLiteDatabase db = this.getReadableDatabase();
        User user = null;

        // Query to get the first (and only) user record
        Cursor cursor = db.query(
                TABLE_USER,
                null, // Select all columns
                null, // No WHERE clause
                null, // No WHERE args
                null, // No GROUP BY
                null, // No HAVING
                COLUMN_ID + " DESC", // ORDER BY id DESC (get latest)
                "1" // LIMIT 1
        );

        try {
            if (cursor != null && cursor.moveToFirst()) {
                user = new User();

                // Extract data from cursor
                user.setUserId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)));
                user.setFullName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FULL_NAME)));
                user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)));
                user.setRole(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ROLE)));

                // Handle nullable station fields
                int stationIdIndex = cursor.getColumnIndexOrThrow(COLUMN_STATION_ID);
                user.setStationId(cursor.isNull(stationIdIndex) ? null : cursor.getString(stationIdIndex));

                int stationNameIndex = cursor.getColumnIndexOrThrow(COLUMN_STATION_NAME);
                user.setStationName(cursor.isNull(stationNameIndex) ? null : cursor.getString(stationNameIndex));

                int stationLocationIndex = cursor.getColumnIndexOrThrow(COLUMN_STATION_LOCATION);
                user.setStationLocation(
                        cursor.isNull(stationLocationIndex) ? null : cursor.getString(stationLocationIndex));

                user.setActive(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_ACTIVE)) == 1);
                user.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT)));

                Log.d(TAG, "User retrieved: " + user.getEmail() + " (Role: " + user.getRole() + ")");
                if (user.getStationId() != null) {
                    Log.d(TAG, "Station: " + user.getStationName() + " (ID: " + user.getStationId() + ")");
                } else {
                    Log.d(TAG, "No station assigned");
                }
            } else {
                Log.d(TAG, "No user found in database");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error retrieving user: " + e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return user;
    }

    /**
     * Check if a user is logged in (exists in database)
     *
     * @return true if user exists, false otherwise
     */
    public boolean isUserLoggedIn() {
        User user = getLoggedInUser();
        boolean loggedIn = user != null;
        Log.d(TAG, "User logged in status: " + loggedIn);
        return loggedIn;
    }

    /**
     * Delete user data (logout)
     *
     * @return true if successful, false otherwise
     */
    public boolean deleteUser() {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            int rowsDeleted = db.delete(TABLE_USER, null, null);
            Log.d(TAG, "User data deleted. Rows affected: " + rowsDeleted);
            return rowsDeleted > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error deleting user: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Update user station information (when admin assigns station)
     *
     * @param userId          User ID (NIC)
     * @param stationId       Station ID
     * @param stationName     Station name
     * @param stationLocation Station location
     * @return true if successful, false otherwise
     */
    public boolean updateUserStation(String userId, String stationId, String stationName, String stationLocation) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_STATION_ID, stationId);
        values.put(COLUMN_STATION_NAME, stationName);
        values.put(COLUMN_STATION_LOCATION, stationLocation);

        try {
            int rowsUpdated = db.update(
                    TABLE_USER,
                    values,
                    COLUMN_USER_ID + " = ?",
                    new String[] { userId });

            if (rowsUpdated > 0) {
                Log.d(TAG, "User station updated: " + stationName + " (ID: " + stationId + ")");
                return true;
            } else {
                Log.e(TAG, "Failed to update user station. User not found.");
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating user station: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Check if operator has station assigned
     *
     * @return true if station is assigned, false otherwise
     */
    public boolean hasStationAssigned() {
        User user = getLoggedInUser();
        if (user == null) {
            Log.d(TAG, "No user found, station not assigned");
            return false;
        }

        boolean hasStation = user.getStationId() != null && !user.getStationId().isEmpty();
        Log.d(TAG, "Station assignment status: " + hasStation);
        return hasStation;
    }

    /**
     * Clear all data from database
     */
    public void clearAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_USER, null, null);
        Log.d(TAG, "All data cleared from database");
    }
}
