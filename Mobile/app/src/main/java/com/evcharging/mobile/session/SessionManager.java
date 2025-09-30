package com.evcharging.mobile.session;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class SessionManager {
    private static final String TAG = "SessionManager";
    private static final String PREF_NAME = "auth_prefs";
    private static final String KEY_TOKEN = "auth_token";

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
}
