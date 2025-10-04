package com.evcharging.mobile.session;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class SessionManager {
    private static final String TAG = "SessionManager";
    private static final String PREF_NAME = "auth_prefs";
    private static final String KEY_TOKEN = "auth_token";
    private static final String KEY_REMEMBER_ME = "remember_me";
    private static final String KEY_SAVED_EMAIL = "saved_email";
    private static final String KEY_SAVED_PASSWORD = "saved_password";

    private SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveToken(String token) {
        prefs.edit().putString(KEY_TOKEN, token).apply();
        Log.d(TAG, "Token saved successfully");
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public void clearToken() {
        prefs.edit().remove(KEY_TOKEN).apply();
        Log.d(TAG, "Token cleared");
    }

    public boolean isLoggedIn() {
        return getToken() != null;
    }

    // Remember Me functionality
    public void saveCredentials(String email, String password, boolean rememberMe) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_REMEMBER_ME, rememberMe);

        if (rememberMe) {
            editor.putString(KEY_SAVED_EMAIL, email);
            editor.putString(KEY_SAVED_PASSWORD, password);
            Log.d(TAG, "Credentials saved for remember me");
        } else {
            editor.remove(KEY_SAVED_EMAIL);
            editor.remove(KEY_SAVED_PASSWORD);
            Log.d(TAG, "Credentials cleared");
        }

        editor.apply();
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

    public void clearAll() {
        prefs.edit().clear().apply();
        Log.d(TAG, "All session data cleared");
    }
}
