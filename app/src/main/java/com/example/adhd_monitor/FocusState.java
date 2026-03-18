// com/example/adhd_monitor/focus/FocusState.java
package com.example.adhd_monitor;

import android.content.Context;
import android.content.SharedPreferences;

public final class FocusState {
    private static final String PREF = "focus_prefs";
    private static final String KEY_ACTIVE = "focus_active";

    public static boolean isActive(Context c){
        return c.getSharedPreferences(PREF, Context.MODE_PRIVATE).getBoolean(KEY_ACTIVE, false);
    }
    public static void setActive(Context c, boolean active){
        SharedPreferences sp = c.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        sp.edit().putBoolean(KEY_ACTIVE, active).apply();
    }
}
