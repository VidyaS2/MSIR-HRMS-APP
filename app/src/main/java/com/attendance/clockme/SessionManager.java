package com.attendance.clockme; // Update this to match your app's package name

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "user_session";
    private static final String KEY_EMAIL = "email";
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    // Constructor
    public SessionManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    // Save the email
    public void saveEmail(String email) {
        editor.putString(KEY_EMAIL, email);
        editor.apply();
    }

    // Retrieve the email
    public String getEmail() {
        return sharedPreferences.getString(KEY_EMAIL, null);
    }

    // Clear the session (e.g., on logout)
    public void clearSession() {
        editor.clear();
        editor.apply();
    }
}