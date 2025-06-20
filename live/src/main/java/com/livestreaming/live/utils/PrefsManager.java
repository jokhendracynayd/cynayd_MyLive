package com.livestreaming.live.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefsManager {
    private static final String PREF_NAME = "my_app_prefs";
    private static SharedPreferences prefs;
    private static SharedPreferences.Editor editor;

    public static void init(Context context) {
        if (prefs == null) {
            prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            editor = prefs.edit();
        }
    }

    public static void saveNotificationObject(String jsonString) {
        editor.putString("notification_object", jsonString);
        editor.apply(); // or editor.commit();
    }

    public static String getNotificationObject() {
        return prefs.getString("notification_object", null);
    }
}

