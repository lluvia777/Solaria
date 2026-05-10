package com.example.solaria.storage;

import android.content.Context;
import android.content.SharedPreferences;

public class CacheManager {
    private static final String PREF_NAME = "SolariaCache";
    private static final String KEY_UV_INDEX = "last_uv_index";

    public static void saveLastUVIndex(Context context, double uvIndex) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat(KEY_UV_INDEX, (float) uvIndex);
        editor.apply();
    }

    public static double getLastUVIndex(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return preferences.getFloat(KEY_UV_INDEX, -1);
    }
}