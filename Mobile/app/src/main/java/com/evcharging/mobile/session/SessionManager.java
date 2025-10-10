package com.evcharging.mobile.session;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.evcharging.mobile.database.DatabaseHelper;
import com.evcharging.mobile.model.User;
import com.evcharging.mobile.utils.JwtUtils;

public class SessionManager {
    private static final String TAG = "SessionManager";
    private static final String PREF_NAME = "auth_prefs";
    private static final String KEY_TOKEN = "auth_token";
    private static final String KEY_REMEMBER_ME = "remember_me";
    private static final String KEY_SAVED_EMAIL = "saved_email";
    private static final String KEY_SAVED_PASSWORD = "saved_password";
    private static final String KEY_USER_ID = "userId";

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private DatabaseHelper dbHelper;
    private Context context;

    public SessionManager(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    // -------------------- TOKEN MANAGEMENT --------------------

    public void saveToken(String token) {
        editor.putString(KEY_TOKEN, token);
        editor.apply();
        Log.d(TAG, "Token saved successfully");

        saveUserFromToken(token);
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public void clearToken() {
        editor.remove(KEY_TOKEN).apply();
        Log.d(TAG, "Token cleared");
    }

    // -------------------- USER EXTRACTION --------------------

    private void saveUserFromToken(String token) {
        try {
            User user = JwtUtils.getUserFromToken(token);
            if (user == null) {
                Log.e(TAG, "Failed to extract user from token");
                return;
            }

            Log.d("LOGIN_SUCCESS", "----------------------------------------");
            Log.d("LOGIN_SUCCESS", "User Full Name : " + user.getFullName());
            Log.d("LOGIN_SUCCESS", "Email          : " + user.getEmail());
            Log.d("LOGIN_SUCCESS", "Role           : " + user.getRole());
            Log.d("LOGIN_SUCCESS", "Is Active      : " + user.isActive());
            Log.d("LOGIN_SUCCESS", "Station ID     : " + user.getStationId());
            Log.d("LOGIN_SUCCESS", "Station Name   : " + user.getStationName());
            Log.d("LOGIN_SUCCESS", "Station Loc.   : " + user.getStationLocation());
            Log.d("LOGIN_SUCCESS", "----------------------------------------");

            boolean saved = dbHelper.saveUser(user);
            if (saved) {
                Log.d(TAG, "User data saved to database successfully");
            } else {
                Log.e(TAG, "Failed to save user data to database");
            }

        } catch (Exception e) {
            Log.e(TAG, "Error saving user from token: " + e.getMessage(), e);
        }
    }

    // -------------------- LOGIN STATUS --------------------

    public boolean isLoggedIn() {
        String token = getToken();
        boolean hasToken = token != null;
        boolean hasUserInDb = dbHelper.isUserLoggedIn();

        Log.d(TAG, "Login check → token=" + hasToken + ", userInDb=" + hasUserInDb);
        return hasToken && hasUserInDb;
    }

    public User getLoggedInUser() {
        return dbHelper.getLoggedInUser();
    }

    public boolean isOperator() {
        User user = getLoggedInUser();
        return user != null && user.isOperator();
    }

    public boolean isOwner() {
        User user = getLoggedInUser();
        return user != null && user.isOwner();
    }

    public boolean hasStationAssigned() {
        User user = getLoggedInUser();
        return user != null && user.hasStationAssigned();
    }

    public String getStationId() {
        User user = getLoggedInUser();
        return user != null ? user.getStationId() : null;
    }

    // -------------------- STATION INFO --------------------

    public boolean updateStationInfo(String stationId, String stationName, String stationLocation) {
        User user = getLoggedInUser();
        if (user == null) return false;

        boolean updated = dbHelper.updateUserStation(user.getUserId(), stationId, stationName, stationLocation);
        if (updated) Log.d(TAG, "Station info updated");
        return updated;
    }

    // -------------------- REMEMBER-ME --------------------

    public void saveCredentials(String email, String password, boolean rememberMe) {
        editor.putBoolean(KEY_REMEMBER_ME, rememberMe);
        if (rememberMe) {
            editor.putString(KEY_SAVED_EMAIL, email);
            editor.putString(KEY_SAVED_PASSWORD, password);
        } else {
            editor.remove(KEY_SAVED_EMAIL);
            editor.remove(KEY_SAVED_PASSWORD);
        }
        editor.apply();
        Log.d(TAG, "Remember-me updated");
    }

    public boolean isRememberMeEnabled() {
        return prefs.getBoolean(KEY_REMEMBER_ME, false);
    }

    public String getSavedEmail() {
        return prefs.getString(KEY_SAVED_EMAIL, "");
    }

    public String getSavedPassword() {
        return prefs.getString(KEY_SAVED_PASSWORD, "");
    }

    // -------------------- CLEAR / LOGOUT --------------------

    public void clearAll() {
        editor.clear().apply();
        dbHelper.deleteUser();
        Log.d(TAG, "Session cleared completely");
    }

    public void logout(boolean clearRememberMe) {
        clearToken();
        dbHelper.deleteUser();
        if (clearRememberMe) {
            editor.remove(KEY_REMEMBER_ME)
                    .remove(KEY_SAVED_EMAIL)
                    .remove(KEY_SAVED_PASSWORD)
                    .apply();
        }
        Log.d(TAG, "User logged out");
    }

    // -------------------- USER ID --------------------

    public String getUserId() {
        User user = getLoggedInUser();
        return user != null ? user.getUserId() : prefs.getString(KEY_USER_ID, "");
    }

    public boolean saveLoggedInUser(User user) {
        if (user == null) return false;
        boolean saved = dbHelper.saveUser(user);
        if (saved) Log.d(TAG, "User saved: " + user.getEmail());
        return saved;
    }
}
