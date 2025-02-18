package com.attendance.clockme;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class ResetAlarmReceiver extends BroadcastReceiver {

    private static final String PREFS_NAME = "ClockPrefs";
    private static final String CLOCK_IN_KEY = "clockInTime";
    private static final String CLOCK_OUT_KEY = "clockOutTime";

    @Override
    public void onReceive(Context context, Intent intent) {
        // Reset clock times to 00:00 at 7 AM
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(CLOCK_IN_KEY, "00:00");
        editor.putString(CLOCK_OUT_KEY, "00:00");
        editor.apply();
    }
}
